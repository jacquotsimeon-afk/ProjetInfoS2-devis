/* =========================================================================
 * NOM DE LA CLASSE : Batiment
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Abstraite (Modèle parent)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * C'est le "moule" global du projet. Elle impose aux classes enfants 
 * (Maison et Immeuble) de posséder des méthodes pour calculer leur surface et prix.
 * =========================================================================
 * @author Clémentine 
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

// Le mot-clé "abstract" signifie qu'on ne peut pas créer un simple "Batiment". 
// Il faut obligatoirement créer une de ses sous-classes (Maison ou Immeuble).
public abstract class Batiment {
    
    // "protected" permet aux sous classes héritant de Batiment d'accéder directement à ces variables
    protected String idBatiment;
    protected int nbrNiveaux;

    // Méthodes abstraites : à définir par les sous classes, le code de ces calculs
    public abstract double devisBatiment();
    public abstract double surfaceSolBatiment(); 
    
    // Getter pour récupérer le nom du bâtiment
    public String getId() { return idBatiment; }
}