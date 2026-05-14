package com.tbcare.common.dto;

public class DashboardStats {
    private long activePatientsCount;
    private long appointmentsTodayCount;
    private long appointmentsThisWeekCount;
    private long pendingAppointmentsCount;

    public long getActivePatientsCount() {
        return activePatientsCount;
    }

    public void setActivePatientsCount(long activePatientsCount) {
        this.activePatientsCount = activePatientsCount;
    }

    public long getAppointmentsTodayCount() {
        return appointmentsTodayCount;
    }

    public void setAppointmentsTodayCount(long appointmentsTodayCount) {
        this.appointmentsTodayCount = appointmentsTodayCount;
    }

    public long getAppointmentsThisWeekCount() {
        return appointmentsThisWeekCount;
    }

    public void setAppointmentsThisWeekCount(long appointmentsThisWeekCount) {
        this.appointmentsThisWeekCount = appointmentsThisWeekCount;
    }

    public long getPendingAppointmentsCount() {
        return pendingAppointmentsCount;
    }

    public void setPendingAppointmentsCount(long pendingAppointmentsCount) {
        this.pendingAppointmentsCount = pendingAppointmentsCount;
    }
}
