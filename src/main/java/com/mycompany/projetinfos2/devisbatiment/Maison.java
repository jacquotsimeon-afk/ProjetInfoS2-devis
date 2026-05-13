package com.mycompany.projetinfos2.devisbatiment;

public class Maison extends Batiment {
    private Piece[] pieces;
    private int nbPiecesRealisees = 0;

    public Maison(String id, int nbrP) {
        this.idBatiment = id; this.pieces = new Piece[nbrP];
    }

    public void ajouterPiece(Piece p) { if (nbPiecesRealisees < pieces.length) pieces[nbPiecesRealisees++] = p; }

    @Override public double devisBatiment() {
        double total = 0;
        for (int i = 0; i < nbPiecesRealisees; i++) total += pieces[i].devisPiece(2.5);
        return total;
    }

    @Override public double surfaceSolBatiment() {
        double total = 0;
        for (int i = 0; i < nbPiecesRealisees; i++) total += pieces[i].surfaceSol();
        return total;
    }
    public Piece[] getPieces() { return pieces; }
}