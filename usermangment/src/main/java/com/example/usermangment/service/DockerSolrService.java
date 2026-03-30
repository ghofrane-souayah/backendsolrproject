package com.example.usermangment.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class DockerSolrService {

    public String buildContainerName(String instanceName) {
        return "solr-" + instanceName.toLowerCase().replaceAll("[^a-z0-9_-]", "-");
    }

    public void startContainer(String instanceName, int hostPort) {
        String containerName = buildContainerName(instanceName);

        runCommand("docker", "rm", "-f", containerName);

        runCommand(
                "docker", "run", "-d",
                "--name", containerName,
                "-p", hostPort + ":8983",
                "solr:9"
        );
    }

    public void stopAndRemoveContainer(String instanceName) {
        String containerName = buildContainerName(instanceName);
        runCommand("docker", "rm", "-f", containerName);
    }

    private void runCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Docker command failed: " + output);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'exécution Docker", e);
        }
    }
}