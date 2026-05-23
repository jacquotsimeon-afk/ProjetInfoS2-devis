/* =========================================================================
 * NOM DE LA CLASSE : Peinture
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Sous-classe (Héritage de Revetement)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Produit spécifique du catalogue.
 * Hérite de Revetement et gère son propre calcul de prix selon la surface.
 * =========================================================================
 * @author Clémentine 
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Peinture extends Revetement {
    
    // Constructeur qui initialise toutes les propriétés héritées de la classe mère
    public Peinture(int id, String nom, double p, boolean m, boolean s, boolean pl) {
        this.idRevetement = id; 
        this.designation = nom; 
        this.prix = p;
        // Est ce que c'est pour mur/sol/plafond ?
        this.pourMur = m; 
        this.pourSol = s; 
        this.pourPlafond = pl;
    }
    
    // Redéfinition du calcul de prix : Surface nette à peindre multipliée par le prix au m²
    @Override 
    public double montant(double surface) { 
        return surface * prix; 
    }
}