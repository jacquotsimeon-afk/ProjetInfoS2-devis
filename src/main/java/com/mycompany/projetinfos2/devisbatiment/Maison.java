/* =========================================================================
 * NOM DE LA CLASSE : Maison
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Sous-classe (Héritage de Batiment)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente un bâtiment simple de plain-pied (sans étages).
 * Elle contient directement ses pièces et calcule son devis en les additionnant.
 * =========================================================================
 * @author Clémentine 
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Maison extends Batiment {
    // Tableau contenant directement les pièces (contrairement à l'immeuble qui a des étages)
    private Piece[] pieces;
    private int nbPiecesRealisees = 0;

    // Constructeur de la maison
    public Maison(String id, int nbrP) {
        this.idBatiment = id; 
        this.pieces = new Piece[nbrP];
    }

    // Ajoute une pièce si le tableau n'est pas rempli
    public void ajouterPiece(Piece p) { 
        if (nbPiecesRealisees < pieces.length) {
            pieces[nbPiecesRealisees] = p;
            nbPiecesRealisees++;
        }
    }
    
    // Utile si on veut annuler la création d'une pièce
    public void supprimerDernierePiece() {
        if (nbPiecesRealisees > 0) {
            nbPiecesRealisees--;
            pieces[nbPiecesRealisees] = null; // Libère la mémoire
        }
    }
    
    // Redéfinition obligatoire de la méthode abstraite de la classe mère
    @Override 
    public double devisBatiment() {
        double total = 0;
        // On additionne le prix de chaque pièce (avec une hauteur sous plafond par défaut de 2.5m)
        for (int i = 0; i < nbPiecesRealisees; i++) total += pieces[i].devisPiece(2.5);
        return total;
    }

    // Calcule la surface au sol totale de la maison
    @Override 
    public double surfaceSolBatiment() {
        double total = 0;
        for (int i = 0; i < nbPiecesRealisees; i++) total += pieces[i].surfaceSol();
        return total;
    }
    
    public Piece[] getPieces() { return pieces; }
}