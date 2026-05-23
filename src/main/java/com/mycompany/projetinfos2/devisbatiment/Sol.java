/* =========================================================================
 * NOM DE LA CLASSE : Sol
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Métier (Composant d'une pièce)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente la surface inférieure (plancher) d'une pièce.
 * Gère ses revêtements et ses trémies pour calculer le devis net.
 * =========================================================================
 * @author Clémentine 
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Sol {
    private Revetement revetement;
    // Tableau des ouvertures dans le sol (trémies)
    private Tremie[] tremies = new Tremie[10];
    private int nbT = 0;

    public void ajouterTremie(Tremie t) { 
        if (nbT < tremies.length) tremies[nbT++] = t; 
    }
    
    public void appliquerRevetement(Revetement r) { 
        this.revetement = r; 
    }
    
    // Déduit la surface des trémies de la surface brute pour obtenir la surface facturable
    public double surfaceNette(double brute) {
        double s = brute;
        for (int i = 0; i < nbT; i++) s -= tremies[i].surface();
        return Math.max(0, s);
    }

    // Calcule le prix du revêtement appliqué au sol
    public double devis(double sBrute) {
        return (revetement != null) ? revetement.montant(surfaceNette(sBrute)) : 0;
    }
    
    // Getters
    public Revetement getRevetement() { return revetement; }
    public Tremie[] getTremies() { return tremies; }
    public int getNbT() { return nbT; }
}