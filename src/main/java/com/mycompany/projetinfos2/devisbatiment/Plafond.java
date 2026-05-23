/* =========================================================================
 * NOM DE LA CLASSE : Plafond
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Métier (Composant d'une pièce)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente la surface supérieure d'une pièce.
 * Peut recevoir un revêtement et contenir des trémies (ex: passage d'escalier).
 * =========================================================================
 * @author Clémentine 
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Plafond {
    // Le matériau appliqué sur le plafond (peinture, plâtre...)
    private Revetement revetement;
    // Tableau des trous dans le plafond (trémies)
    private Tremie[] tremies = new Tremie[10];
    private int nbT = 0;

    public void ajouterTremie(Tremie t) { 
        if (nbT < tremies.length) tremies[nbT++] = t; 
    }
    
    public void appliquerRevetement(Revetement r) { 
        this.revetement = r; 
    }
    
    // Calcule la surface réellement facturable en déduisant les trous
    public double surfaceNette(double brute) {
        double s = brute;
        for (int i = 0; i < nbT; i++) s -= tremies[i].surface();
        return Math.max(0, s); // Sécurité anti-négatif
    }

    // Calcule le prix du plafond
    public double devis(double sBrute) {
        // Si aucun revêtement n'est appliqué, ça coûte 0€
        return (revetement != null) ? revetement.montant(surfaceNette(sBrute)) : 0;
    }
    
    // Getters
    public Revetement getRevetement() { return revetement; }
    public int getNbT() { return nbT; }
}