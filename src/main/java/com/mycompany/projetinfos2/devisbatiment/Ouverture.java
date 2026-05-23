/* =========================================================================
 * NOM DE LA CLASSE : Ouverture
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Abstraite (Modèle parent)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Modèle générique représentant un "trou" dans un mur ou un sol.
 * Force ses sous-classes (Porte, Fenêtre, Trémie) à définir leur propre surface.
 * =========================================================================
 * @author Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public abstract class Ouverture {
    // Méthode abstraite : chaque type d'ouverture devra fournir sa formule de surface
    public abstract double surface();
    // Méthode abstraite : chaque ouverture devra pouvoir donner son nom (ex: "Porte")
    public abstract String getNom();
}