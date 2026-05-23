/* =========================================================================
 * NOM DE LA CLASSE : Appartement
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Métier (Composant du bâtiment)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente un appartement qui sert de conteneur pour un ensemble de pièces.
 * Permet de faire la somme des surfaces et des devis de toutes ses pièces.
 * =========================================================================
 * @author Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Appartement {
    // Identifiant unique de l'appartement
    private int idAppart;
    // Tableau fixe contenant les pièces qui composent cet appartement
    private Piece[] pieces;
    // Compteur pour savoir combien de pièces ont réellement été ajoutées dans le tableau
    private int nbPiecesRealisees = 0;

    // Constructeur : on définit l'ID et la taille maximum du tableau de pièces
    public Appartement(int id, int nbrTotalPieces) {
        this.idAppart = id;
        this.pieces = new Piece[nbrTotalPieces];
    }

    // Ajoute une pièce à l'appartement (si le tableau n'est pas déjà plein)
    public void ajouterPiece(Piece p) {
        if (nbPiecesRealisees < pieces.length) {
            pieces[nbPiecesRealisees] = p;
            nbPiecesRealisees++;
        }
    }
    
    // Supprime la dernière pièce ajoutée (utile en cas d'erreur de saisie)
    public void supprimerDernierePiece() {
        if (nbPiecesRealisees > 0) {
            nbPiecesRealisees--;
            pieces[nbPiecesRealisees] = null; // On vide la case
        }
    }

    // Calcule le prix total de l'appartement en additionnant le devis de chaque pièce
    public double devisAppartement(double hauteur) {
        double total = 0;
        for (int i = 0; i < nbPiecesRealisees; i++) {
            total += pieces[i].devisPiece(hauteur);
        }
        return total;
    }

    // Calcule la surface totale au sol en additionnant la surface de chaque pièce
    public double surfaceSolAppartement() {
        double total = 0;
        for (int i = 0; i < nbPiecesRealisees; i++) {
            total += pieces[i].surfaceSol();
        }
        return total;
    }

    // Getters
    public Piece[] getPieces() { return pieces; }
    public int getId() { return idAppart; }
}