package com.example.usermangment.dto;

public class UserStatsDto {
    public long total;
    public long active;
    public long inactive;

    public UserStatsDto(long total, long active, long inactive) {
        this.total = total;
        this.active = active;
        this.inactive = inactive;
    }
}
