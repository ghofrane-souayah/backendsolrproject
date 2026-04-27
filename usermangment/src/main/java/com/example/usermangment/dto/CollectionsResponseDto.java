package com.example.usermangment.dto;

import java.util.List;

public class CollectionsResponseDto {

    private ServerMiniDto server;
    private List<CollectionInfoDto> collections;

    public CollectionsResponseDto() {
    }

    public CollectionsResponseDto(ServerMiniDto server, List<CollectionInfoDto> collections) {
        this.server = server;
        this.collections = collections;
    }

    public ServerMiniDto getServer() {
        return server;
    }

    public void setServer(ServerMiniDto server) {
        this.server = server;
    }

    public List<CollectionInfoDto> getCollections() {
        return collections;
    }

    public void setCollections(List<CollectionInfoDto> collections) {
        this.collections = collections;
    }

    public static class ServerMiniDto {
        private Long id;
        private String name;
        private String host;
        private Integer port;

        public ServerMiniDto() {
        }

        public ServerMiniDto(Long id, String name, String host, Integer port) {
            this.id = id;
            this.name = name;
            this.host = host;
            this.port = port;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getHost() {
            return host;
        }

        public Integer getPort() {
            return port;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }
}