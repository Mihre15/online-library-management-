package com.library_management.library_project;

import java.time.Instant;
import java.util.Date;

class Health {
    private String status;
    private Date checkedTime;

    public Health(String status) {
        this.status = status;
        this.checkedTime = Date.from(Instant.now());
    }

    Health setStatus(String status) {
        this.status = status;
        return this;
    }

    String getStatus() {
        return this.status;
    }

    Date getCheckedTime() {
        return this.checkedTime;
    }
}