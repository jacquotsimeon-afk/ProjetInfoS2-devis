package com.mycompany.projetinfos2.devisbatiment;

public class Plafond {
    private Revetement revetement;
    private Tremie[] tremies = new Tremie[10];
    private int nbT = 0;

    public void ajouterTremie(Tremie t) { if (nbT < tremies.length) tremies[nbT++] = t; }
    public void appliquerRevetement(Revetement r) { this.revetement = r; }
    
    public double surfaceNette(double brute) {
        double s = brute;
        for (int i = 0; i < nbT; i++) s -= tremies[i].surface();
        return Math.max(0, s);
    }

    public double devis(double sBrute) {
        return (revetement != null) ? revetement.montant(surfaceNette(sBrute)) : 0;
    }
    public Revetement getRevetement() { return revetement; }
    public int getNbT() { return nbT; }
}