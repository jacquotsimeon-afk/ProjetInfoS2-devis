/* =========================================================================
 * NOM DE LA CLASSE : Immeuble
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Sous-classe (Héritage de Batiment)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente un bâtiment à plusieurs étages. Il contient un tableau de "Niveaux".
 * Calcule son prix total en additionnant les devis de chacun de ses étages.
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Immeuble extends Batiment {
    // Tableau fixe pour stocker les différents étages
    private Niveau[] niveaux;
    // Compteur pour savoir combien de niveaux ont été créés pour l'instant
    private int nbNiveauxRealises = 0;

    // Constructeur : initialise le nom et la taille maximale du tableau d'étages
    public Immeuble(String id, int nbrN) {
        this.idBatiment = id; 
        this.niveaux = new Niveau[nbrN];
    }

    // Ajoute un niveau dans la première case vide du tableau
    public void ajouterNiveau(Niveau n) { 
        if (nbNiveauxRealises < niveaux.length) {
            niveaux[nbNiveauxRealises] = n;
            nbNiveauxRealises++;
        }
    }

    // Redéfinition obligatoire de la méthode abstraite de la classe mère Batiment
    @Override 
    public double devisBatiment() {
        double total = 0;
        // On boucle sur les étages et on demande à chacun de se facturer lui-même
        for (int i = 0; i < nbNiveauxRealises; i++) total += niveaux[i].devisNiveau();
        return total;
    }

    // Calcule la surface au sol de tout l'immeuble (somme de la surface de chaque niveau)
    @Override 
    public double surfaceSolBatiment() {
        double total = 0;
        for (int i = 0; i < nbNiveauxRealises; i++) total += niveaux[i].surfaceSolNiveau();
        return total;
    }
    
    // Getter pour récupérer le tableau (très utile pour l'interface graphique)
    public Niveau[] getNiveaux() { return niveaux; }
}