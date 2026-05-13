package com.mycompany.projetinfos2.devisbatiment;

public class Niveau {
    private int idNiveau;
    private double hauteurPlafond;
    private Appartement[] apparts;
    private int nbA = 0;

    public Niveau(int id, double h, int nbrA) {
        this.idNiveau = id; this.hauteurPlafond = h; this.apparts = new Appartement[nbrA];
    }

    public void ajouterAppart(Appartement a) { if (nbA < apparts.length) apparts[nbA++] = a; }
    
    public double devisNiveau() {
        double t = 0;
        for (int i = 0; i < nbA; i++) t += apparts[i].devisAppartement(hauteurPlafond);
        return t;
    }

    public double surfaceSolNiveau() {
        double t = 0;
        for (int i = 0; i < nbA; i++) t += apparts[i].surfaceSolAppartement();
        return t;
    }
    public double getHauteur() { return hauteurPlafond; }
    public int getId() { return idNiveau; }
    public Appartement[] getApparts() { return apparts; }
}