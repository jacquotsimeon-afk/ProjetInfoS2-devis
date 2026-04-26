/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class Immeuble extends Batiment {
    private Niveau[] niveaux;
    private int nbNiveauxRealises = 0;

    public Immeuble(String id, int nbrN) {
        this.idBatiment = id;
        this.niveaux = new Niveau[nbrN];
        this.nbrNiveaux = nbrN;
    }

    public void ajouterNiveau(Niveau n) {
        if (nbNiveauxRealises < niveaux.length) {
            niveaux[nbNiveauxRealises] = n;
            nbNiveauxRealises++;
        }
    }

    @Override
    public double devisBatiment() {
        double total = 0;
        for (int i = 0; i < nbNiveauxRealises; i++) {
            total += niveaux[i].devisNiveau();
        }
        return total;
    }

    @Override
    public double surfaceSolBatiment() {
        double total = 0;
        for (int i = 0; i < nbNiveauxRealises; i++) {
            total += niveaux[i].surfaceSolNiveau();
        }
        return total;
    }

    public Niveau[] getNiveaux() { 
        return niveaux; 
    }
}