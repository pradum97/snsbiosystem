package com.techwhizer.snsbiosystem.user.model;

 public class RoleConfigModel {

        private boolean admin, doctor, patient, dealer , guest;

     public boolean isGuest() {
         return guest;
     }

     public void setGuest(boolean guest) {
         this.guest = guest;
     }

     public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }

        public boolean isDoctor() {
            return doctor;
        }

        public void setDoctor(boolean doctor) {
            this.doctor = doctor;
        }

        public boolean isPatient() {
            return patient;
        }

        public void setPatient(boolean patient) {
            this.patient = patient;
        }

        public boolean isDealer() {
            return dealer;
        }

        public void setDealer(boolean dealer) {
            this.dealer = dealer;
        }
    }