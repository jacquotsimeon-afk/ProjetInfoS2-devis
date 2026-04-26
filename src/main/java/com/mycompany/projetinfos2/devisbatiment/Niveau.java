/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class Niveau {
    private int idNiveau;
    private double hauteurPlafond;
    private Appartement[] apparts;
    private int nbA = 0;

    public Niveau(int id, double h, int nbrA) {
        this.idNiveau = id; this.hauteurPlafond = h;
        this.apparts = new Appartement[nbrA];
    }

    public void ajouterAppart(Appartement a) { if (nbA < apparts.length) apparts[nbA++] = a; }
    public double devisNiveau() {
        double total = 0;
        for (int i = 0; i < nbA; i++) total += apparts[i].devisAppartement(hauteurPlafond);
        return total;
    }
    
    public double surfaceSolNiveau() {
    double total = 0;
    for (int i = 0; i < nbA; i++) {
        total += apparts[i].surfaceSolAppartement();
    }
    return total;
    }
    
    public double getHauteur() { return hauteurPlafond; }
    public Appartement[] getApparts() { return apparts; }
    public int getId() { return idNiveau; }
}