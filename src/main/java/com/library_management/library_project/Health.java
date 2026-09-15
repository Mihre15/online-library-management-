package com.library_management.library_project;

class Health {
    private String status;

    public Health(String status) {
        this.status = status;
    }

    Health setStatus(String status) {
        this.status = status;
        return this;
    }

    String getStatus() {
        return this.status;
    }
}