package com.projects.cinephiles.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BookingSuccessResponse {
  private Boolean success;
  private String message;
  private String movieTitle;
  private String moviePoster;
  private String BookingId;
  private String movieCertification;
  private String theatre;
  private String location;
  private String theatreCity;
  private String screenName;
  private String seatIds;
  private String showFormat;
  private String showTime;
  private String showDate;
}
