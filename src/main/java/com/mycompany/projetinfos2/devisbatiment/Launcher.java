/* =========================================================================
 * NOM DE LA CLASSE : Launcher
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Point d'entrée du programme (Méthode Main)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Astuce technique pour lancer JavaFX proprement sans erreur de modules.
 * C'est le vrai point de départ de l'application lorsqu'on crée un fichier exécutable (.jar).
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Launcher {
    
    // La méthode "main" classique par laquelle Java démarre toujours
    public static void main(String[] args) {
        // On appelle la méthode main de notre interface graphique.
        // Cette séparation permet à Java de charger les composants visuels en arrière-plan.
        AppGraphique.main(args);
    }
}