/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class Maison extends Batiment {
    private Piece[] pieces;
    private int nbPiecesRealisees = 0;

    public Maison(String id, int nbrP) {
        this.idBatiment = id;
        this.pieces = new Piece[nbrP];
        this.nbrNiveaux = 1; // Une maison classique a 1 seul niveau principal ici
    }

    public void ajouterPiece(Piece p) {
        if (nbPiecesRealisees < pieces.length) {
            pieces[nbPiecesRealisees] = p;
            nbPiecesRealisees++;
        }
    }

    @Override
    public double devisBatiment() {
        double total = 0;
        // On passe 2.5 mètres comme hauteur sous plafond standard pour les murs de la maison
        for (int i = 0; i < nbPiecesRealisees; i++) {
            total += pieces[i].devisPiece(2.5);
        }
        return total;
    }

    @Override
    public double surfaceSolBatiment() {
        double total = 0;
        for (int i = 0; i < nbPiecesRealisees; i++) {
            total += pieces[i].surfaceSol();
        }
        return total;
    }

    public Piece[] getPieces() { 
        return pieces; 
    }
}