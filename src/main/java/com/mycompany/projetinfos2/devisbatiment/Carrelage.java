/* =========================================================================
 * NOM DE LA CLASSE : Carrelage
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Sous-classe (Héritage)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Définit un revêtement spécifique de type carrelage. 
 * Hérite des attributs de la classe mère Revetement et gère son calcul de prix.
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Carrelage extends Revetement {
    
    // Constructeur qui remplit les variables héritées de la classe mère Revetement
    public Carrelage(int id, String nom, double p, boolean m, boolean s, boolean pl) {
        this.idRevetement = id;
        this.designation = nom;
        this.prix = p;
        this.pourMur = m;      // Est-ce qu'on peut le mettre au mur ?
        this.pourSol = s;      // Est-ce qu'on peut le mettre au sol ?
        this.pourPlafond = pl; // Est-ce qu'on peut le mettre au plafond ?
    }

    // On remplace la méthode de la classe mère pour calculer le prix brut
    @Override
    public double montant(double surface) {
        return surface * prix; // Prix total = surface en m² multipliée par le prix au m²
    }
}