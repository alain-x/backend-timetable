package com.digital_timetable.dto;

import jakarta.validation.constraints.NotBlank;

public class RoomDto {
    private Long id;
    
    @NotBlank(message = "Room name is required")
    private String room_name;
    
    @NotBlank(message = "Block name is required")
    private String block_name;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    private int capacity;
    
    private boolean isBooked;
    // Optional: name of the class representative who booked the room (if booked)
    private String bookedByClassRepName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoom_name() { return room_name; }
    public void setRoom_name(String room_name) { this.room_name = room_name; }
    public String getBlock_name() { return block_name; }
    public void setBlock_name(String block_name) { this.block_name = block_name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }
    public String getBookedByClassRepName() { return bookedByClassRepName; }
    public void setBookedByClassRepName(String bookedByClassRepName) { this.bookedByClassRepName = bookedByClassRepName; }
} 