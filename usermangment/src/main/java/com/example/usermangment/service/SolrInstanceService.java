package com.example.usermangment.service;

import com.example.usermangment.dto.CopySolrInstanceRequest;
import com.example.usermangment.dto.CreateSolrInstanceRequest;
import com.example.usermangment.dto.SolrInstanceDto;
import com.example.usermangment.dto.UpdateSolrInstanceRequest;
import com.example.usermangment.model.Company;
import com.example.usermangment.model.Role;
import com.example.usermangment.model.SolrInstance;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.CompanyRepository;
import com.example.usermangment.repository.SolrInstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SolrInstanceService {

    private static final String DEFAULT_SOLR_IMAGE = "solr:9.10.1";

    private final SolrInstanceRepository solrRepo;
    private final CompanyRepository companyRepo;

    public SolrInstanceService(SolrInstanceRepository solrRepo, CompanyRepository companyRepo) {
        this.solrRepo = solrRepo;
        this.companyRepo = companyRepo;
    }

    private boolean isSuperAdmin(User u) {
        return u != null && u.getRole() == Role.SUPER_ADMIN;
    }

    private boolean isAdmin(User u) {
        return u != null && (u.getRole() == Role.ADMIN || u.getRole() == Role.SUPER_ADMIN);
    }

    private SolrInstanceDto toDto(SolrInstance s) {
        SolrInstanceDto d = new SolrInstanceDto();
        d.setId(s.getId());
        d.setName(s.getName());
        d.setHost(s.getHost());
        d.setPort(s.getPort());
        d.setStatus(s.getStatus());
        d.setCreatedAt(s.getCreatedAt());
        d.setCompanyId(s.getCompany() != null ? s.getCompany().getId() : null);
        return d;
    }

    private boolean isPortInUse(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String sanitizeDockerName(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_-]", "-");
    }

    private String resolveImage(String imagePath) {
        return DEFAULT_SOLR_IMAGE;
    }

    private void runCommandOrThrow(ProcessBuilder pb, String errorMessage) throws Exception {
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        System.out.println("COMMAND OUTPUT: " + output);
        System.out.println("EXIT CODE: " + exitCode);

        if (exitCode != 0) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    errorMessage + ": " + output
            );
        }
    }

    private void runCommandIgnoreFailure(ProcessBuilder pb) {
        try {
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();

            System.out.println("IGNORE COMMAND OUTPUT: " + output);
            System.out.println("IGNORE EXIT CODE: " + exitCode);
        } catch (Exception e) {
            System.out.println("IGNORE COMMAND ERROR: " + e.getMessage());
        }
    }

    private boolean waitUntilSolrReady(String host, int port, int maxAttempts, long delayMs) {
        String url = "http://" + host + ":" + port + "/solr/admin/info/system?wt=json";

        for (int i = 0; i < maxAttempts; i++) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(3000);
                con.setReadTimeout(3000);

                int code = con.getResponseCode();
                if (code >= 200 && code < 300) {
                    return true;
                }
            } catch (Exception ignored) {
            }

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    private void checkSameCompanyOrSuperAdmin(SolrInstance s, User u) {
        if (isSuperAdmin(u)) {
            return;
        }

        if (u == null || u.getCompany() == null || s.getCompany() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        if (!u.getCompany().getId().equals(s.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    // LIST
    public List<SolrInstanceDto> listFor(User u) {
        if (isSuperAdmin(u)) {
            return solrRepo.findAll().stream().map(this::toDto).toList();
        }

        if (u == null || u.getCompany() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no company");
        }

        return solrRepo.findByCompanyId(u.getCompany().getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    // CREATE
    public SolrInstanceDto create(CreateSolrInstanceRequest req, User u) {
        if (!isAdmin(u)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN or SUPER_ADMIN can create solr instances"
            );
        }

        Long companyId;

        if (isSuperAdmin(u)) {
            companyId = req.getCompanyId();
            if (companyId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "companyId is required");
            }
        } else {
            if (u.getCompany() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin has no company");
            }
            companyId = u.getCompany().getId();
        }

        Company c = companyRepo.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        String name = req.getName().trim();
        String host = req.getHost().trim();
        Integer port = req.getPort();
        String image = DEFAULT_SOLR_IMAGE;

        if (solrRepo.existsByCompanyIdAndNameIgnoreCase(companyId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Instance name already exists");
        }

        if (solrRepo.existsByCompanyIdAndHostAndPort(companyId, host, port)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Host + port already exists");
        }

        if (isPortInUse(host, port)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Port already used on the server");
        }

        String volumeName = "solr_data_" + sanitizeDockerName(name);
        String containerName = "solr-" + sanitizeDockerName(name);

        SolrInstance s = new SolrInstance();
        s.setName(name);
        s.setHost(host);
        s.setPort(port);
        s.setStatus("STARTING");
        s.setCompany(c);
        s.setInstancePath(volumeName);
        s.setCorePath(volumeName);
        s.setImagePath(image);

        SolrInstance saved = solrRepo.save(s);

        try {
            runCommandOrThrow(
                    new ProcessBuilder("docker", "volume", "create", volumeName),
                    "Failed to create docker volume"
            );

            runCommandOrThrow(
                    new ProcessBuilder(
                            "docker", "run", "-d",
                            "--name", containerName,
                            "-p", port + ":8983",
                            "-v", volumeName + ":/var/solr",
                            image
                    ),
                    "Failed to start docker container"
            );

            boolean ready = waitUntilSolrReady(host, port, 30, 2000);

            if (!ready) {
                saved.setStatus("DOWN");
                solrRepo.save(saved);

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Solr container started but the instance is not ready yet"
                );
            }

            saved.setStatus("UP");
            return toDto(solrRepo.save(saved));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            saved.setStatus("DOWN");
            solrRepo.save(saved);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while creating and starting instance: " + e.getMessage()
            );
        }
    }

    // UPDATE
    public SolrInstanceDto update(Long id, UpdateSolrInstanceRequest req, User u) {
        if (!isAdmin(u)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN or SUPER_ADMIN can update solr instances"
            );
        }

        SolrInstance s = solrRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solr instance not found"));

        checkSameCompanyOrSuperAdmin(s, u);

        if (req.getName() != null && !req.getName().isBlank()) {
            s.setName(req.getName().trim());
        }

        if (req.getHost() != null && !req.getHost().isBlank()) {
            s.setHost(req.getHost().trim());
        }

        if (req.getPort() != null) {
            s.setPort(req.getPort());
        }

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            s.setStatus(req.getStatus().trim().toUpperCase());
        }

        if (req.getInstancePath() != null && !req.getInstancePath().isBlank()) {
            s.setInstancePath(req.getInstancePath().trim());
        }

        if (req.getCorePath() != null && !req.getCorePath().isBlank()) {
            s.setCorePath(req.getCorePath().trim());
        }

        if (req.getImagePath() != null && !req.getImagePath().isBlank()) {
            s.setImagePath(DEFAULT_SOLR_IMAGE);
        }

        return toDto(solrRepo.save(s));
    }

    // DELETE
    public void delete(Long id, User u) {
        if (!isAdmin(u)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN or SUPER_ADMIN can delete solr instances"
            );
        }

        SolrInstance s = solrRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solr instance not found"));

        checkSameCompanyOrSuperAdmin(s, u);

        String containerName = "solr-" + sanitizeDockerName(s.getName());
        String volumeName = (s.getInstancePath() == null || s.getInstancePath().isBlank())
                ? null
                : s.getInstancePath().trim();

        runCommandIgnoreFailure(
                new ProcessBuilder("docker", "rm", "-f", containerName)
        );

        if (volumeName != null && !volumeName.isBlank()) {
            runCommandIgnoreFailure(
                    new ProcessBuilder("docker", "volume", "rm", "-f", volumeName)
            );
        }

        solrRepo.delete(s);
    }

    // COPY
    public SolrInstanceDto copy(Long id, CopySolrInstanceRequest req, User u) {
        if (!isAdmin(u)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN or SUPER_ADMIN can copy solr instances"
            );
        }

        SolrInstance original = solrRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solr instance not found"));

        if (!isSuperAdmin(u)) {
            if (u.getCompany() == null || original.getCompany() == null ||
                    !u.getCompany().getId().equals(original.getCompany().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }

        String newName = req.getNewName().trim();
        Integer newPort = req.getNewPort();

        Long companyId = original.getCompany().getId();

        if (solrRepo.existsByCompanyIdAndNameIgnoreCase(companyId, newName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Instance name already exists");
        }

        if (solrRepo.existsByCompanyIdAndHostAndPort(companyId, original.getHost(), newPort)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Host + port already exists");
        }

        if (isPortInUse(original.getHost(), newPort)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Port already used on the server");
        }

        if (original.getInstancePath() == null || original.getInstancePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Original docker volume is missing");
        }

        String sourceVolume = original.getInstancePath().trim();
        String targetVolume = "solr_data_" + sanitizeDockerName(newName);
        String containerName = "solr-" + sanitizeDockerName(newName);
        String image = DEFAULT_SOLR_IMAGE;

        SolrInstance clone = new SolrInstance();
        clone.setName(newName);
        clone.setHost(original.getHost());
        clone.setPort(newPort);
        clone.setStatus("DOWN");
        clone.setCompany(original.getCompany());
        clone.setInstancePath(targetVolume);
        clone.setCorePath(targetVolume);
        clone.setImagePath(image);

        try {
            // nettoyage des restes d'une ancienne tentative
            runCommandIgnoreFailure(
                    new ProcessBuilder("docker", "rm", "-f", containerName)
            );
            runCommandIgnoreFailure(
                    new ProcessBuilder("docker", "volume", "rm", "-f", targetVolume)
            );

            // créer le volume cible
            runCommandOrThrow(
                    new ProcessBuilder("docker", "volume", "create", targetVolume),
                    "Failed to create docker volume"
            );

            // copier le contenu depuis le volume source
            runCommandOrThrow(
                    new ProcessBuilder(
                            "docker", "run", "--rm",
                            "-v", sourceVolume + ":/from",
                            "-v", targetVolume + ":/to",
                            "alpine",
                            "sh", "-c", "cp -a /from/. /to/"
                    ),
                    "Failed to copy docker volume content"
            );

            // lancer le nouveau conteneur
            runCommandOrThrow(
                    new ProcessBuilder(
                            "docker", "run", "-d",
                            "--name", containerName,
                            "-p", newPort + ":8983",
                            "-v", targetVolume + ":/var/solr",
                            image
                    ),
                    "Failed to start docker container"
            );

            boolean ready = waitUntilSolrReady(clone.getHost(), clone.getPort(), 30, 2000);

            if (!ready) {
                // cleanup si démarrage incomplet
                runCommandIgnoreFailure(
                        new ProcessBuilder("docker", "rm", "-f", containerName)
                );
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Solr container started but the instance is not ready yet"
                );
            }

            clone.setStatus("UP");
            return toDto(solrRepo.save(clone));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            runCommandIgnoreFailure(
                    new ProcessBuilder("docker", "rm", "-f", containerName)
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while copying and starting instance: " + e.getMessage()
            );
        }
    }
    // START
    public SolrInstanceDto start(Long id, User u) {
        if (!isAdmin(u)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN or SUPER_ADMIN can start solr instances"
            );
        }

        SolrInstance s = solrRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solr instance not found"));

        checkSameCompanyOrSuperAdmin(s, u);

        if ("UP".equalsIgnoreCase(s.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solr instance is already running");
        }

        if (s.getPort() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instance port is missing");
        }

        if (s.getInstancePath() == null || s.getInstancePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instance path is missing");
        }

        if (isPortInUse(s.getHost(), s.getPort())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Port already used on the server");
        }

        try {
            String containerName = "solr-" + sanitizeDockerName(s.getName());
            String image = DEFAULT_SOLR_IMAGE;

            runCommandOrThrow(
                    new ProcessBuilder(
                            "docker", "run", "-d",
                            "--name", containerName,
                            "-p", s.getPort() + ":8983",
                            "-v", s.getInstancePath() + ":/var/solr",
                            image
                    ),
                    "Failed to start Docker container"
            );

            boolean ready = waitUntilSolrReady(s.getHost(), s.getPort(), 30, 2000);

            if (!ready) {
                s.setStatus("DOWN");
                solrRepo.save(s);

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Solr container started but the instance is not ready yet"
                );
            }

            s.setStatus("UP");
            s.setImagePath(DEFAULT_SOLR_IMAGE);
            return toDto(solrRepo.save(s));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            s.setStatus("DOWN");
            solrRepo.save(s);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while starting Solr instance: " + e.getMessage()
            );
        }
    }
}