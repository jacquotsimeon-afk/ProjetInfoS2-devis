package com.mycompany.projetinfos2.devisbatiment;

public abstract class Batiment {
    protected String idBatiment;
    protected int nbrNiveaux;

    public abstract double devisBatiment();
    public abstract double surfaceSolBatiment(); // Nouvelle méthode
    public String getId() { return idBatiment; }
}