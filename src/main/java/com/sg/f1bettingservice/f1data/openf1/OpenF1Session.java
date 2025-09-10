package com.sg.f1bettingservice.f1data.openf1;

public record OpenF1Session(
    Integer session_key,
    Integer meeting_key,
    String meeting_name,
    String session_name,
    String session_type,
    String country_name,
    String date_start,
    Integer year) {}
