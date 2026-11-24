package com.transportpro.model;

import com.transportpro.model.enums.StatutMaintenance;
import com.transportpro.model.enums.TypeMaintenance;

import java.time.LocalDate;

/** Table `maintenance`. */
public class Maintenance {

    private int idMaintenance;
    private Bus bus;
    private TypeMaintenance type;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutMaintenance statut;

    public Maintenance() {
    }

    public Maintenance(int idMaintenance, Bus bus, TypeMaintenance type, LocalDate dateDebut,
                       LocalDate dateFin, StatutMaintenance statut) {
        this.idMaintenance = idMaintenance;
        this.bus = bus;
        this.type = type;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
    }

    public int getIdMaintenance() { return idMaintenance; }
    public void setIdMaintenance(int idMaintenance) { this.idMaintenance = idMaintenance; }

    public Bus getBus() { return bus; }
    public void setBus(Bus bus) { this.bus = bus; }

    public TypeMaintenance getType() { return type; }
    public void setType(TypeMaintenance type) { this.type = type; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public StatutMaintenance getStatut() { return statut; }
    public void setStatut(StatutMaintenance statut) { this.statut = statut; }
}
