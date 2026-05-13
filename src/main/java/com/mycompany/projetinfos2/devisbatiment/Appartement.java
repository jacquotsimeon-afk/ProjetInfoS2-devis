package com.mycompany.projetinfos2.devisbatiment;

public class Appartement {
    private int idAppart;
    private Piece[] pieces;
    private int nbPiecesRealisees = 0;

    public Appartement(int id, int nbrTotalPieces) {
        this.idAppart = id;
        this.pieces = new Piece[nbrTotalPieces];
    }

    public void ajouterPiece(Piece p) {
        if (nbPiecesRealisees < pieces.length) {
            pieces[nbPiecesRealisees] = p;
            nbPiecesRealisees++;
        }
    }

    public double devisAppartement(double hauteur) {
        double total = 0;
        for (int i = 0; i < nbPiecesRealisees; i++) total += pieces[i].devisPiece(hauteur);
        return total;
    }

    public double surfaceSolAppartement() {
        double total = 0;
        for (int i = 0; i < nbPiecesRealisees; i++) total += pieces[i].surfaceSol();
        return total;
    }

    public Piece[] getPieces() { return pieces; }
    public int getId() { return idAppart; }
}