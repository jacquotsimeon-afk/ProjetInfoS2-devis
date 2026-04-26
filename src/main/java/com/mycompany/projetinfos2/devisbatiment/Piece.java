/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class Piece {
    private int idPiece;
    private Mur[] murs;
    private int nbM = 0;
    private Sol sol = new Sol();
    private Plafond plafond = new Plafond();

    public Piece(int id, int nbrMurs) {
        this.idPiece = id;
        this.murs = new Mur[nbrMurs];
    }

    public void ajouterMur(Mur m) { if (nbM < murs.length) murs[nbM++] = m; }
    public double surfaceSol() {
        if (nbM < 2) return 0;
        return murs[0].longueur() * murs[1].longueur();
    }

    public double devisPiece(double h) {
        double total = sol.devis(surfaceSol()) + plafond.devis(surfaceSol());
        for (int i = 0; i < nbM; i++) total += murs[i].devisMur(h);
        return total;
    }

    public Mur[] getMurs() { return murs; }
    public Sol getSol() { return sol; }
    public Plafond getPlafond() { return plafond; }
    public int getId() { return idPiece; }
}