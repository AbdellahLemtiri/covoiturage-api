
INSERT INTO conducteurs (id, nom, prenom, email, telephone, numero_permis)
VALUES (1, 'Alami', 'Hamid', 'hamid.alami@email.ma', '0661112233', 'PERMIS-1001'),
       (2, 'Bennani', 'Youssef', 'youssef.bennani@email.ma', '0661223344', 'PERMIS-1002'),
       (3, 'Tazi', 'Omar', 'omar.tazi@email.ma', '0661334455', 'PERMIS-1003'),
       (4, 'Idrissi', 'Mehdi', 'mehdi.idrissi@email.ma', '0661445566', 'PERMIS-1004'),
       (5, 'Chraibi', 'Amine', 'amine.chraibi@email.ma', '0661556677', 'PERMIS-1005');

INSERT INTO passagers (id, nom, prenom, email, telephone)
VALUES (1, 'Mansouri', 'Salma', 'salma.mansouri@email.ma', '0671112233'),
       (2, 'Fassi', 'Karim', 'karim.fassi@email.ma', '0671223344'),
       (3, 'Berrada', 'Imane', 'imane.berrada@email.ma', '0671334455'),
       (4, 'Ziani', 'Nadia', 'nadia.ziani@email.ma', '0671445566'),
       (5, 'Tahiri', 'Rachid', 'rachid.tahiri@email.ma', '0671556677');

INSERT INTO vehicules (id, marque, modele, immatriculation, couleur, capacite, conducteur_id)
VALUES (1, 'Dacia', 'Logan', '12345-A-1', 'Gris', 4, 1),
       (2, 'Renault', 'Clio 5', '23456-B-6', 'Noir', 4, 2),
       (3, 'Peugeot', '3008', '34567-D-26', 'Blanc', 4, 3),
       (4, 'Volkswagen', 'Golf 8', '45678-A-1', 'Bleu', 4, 4),
       (5, 'Hyundai', 'Tucson', '56789-H-1', 'Gris', 5, 5);

INSERT INTO trajets (id, conducteur_id, vehicule_id, ville_depart, ville_arrivee, date_depart, places_proposees, statut)
VALUES (1, 1, 1, 'Rabat', 'Marrakech', '2026-12-01 17:00:00', 3, 'PUBLIE'),
       (2, 2, 2, 'Casablanca', 'Tanger', '2026-12-02 08:00:00', 4, 'PUBLIE'),
       (3, 3, 3, 'Fes', 'Rabat', '2026-12-03 09:00:00', 3, 'PUBLIE'),
       (4, 4, 4, 'Marrakech', 'Agadir', '2026-12-04 14:00:00', 4, 'PUBLIE'),
       (5, 5, 5, 'Safi', 'Casablanca', '2026-12-05 07:30:00', 4, 'PUBLIE');

INSERT INTO points_arret (id, trajet_id, ville, ordre, heure_passage, prix_depuis_depart)
VALUES
-- Points du Trajet 1 (Hamid: Rabat -> Casa -> Settat -> Marrakech)
(1, 1, 'Rabat', 0, '2026-12-01 17:00:00', 0.00),
(2, 1, 'Casablanca', 1, '2026-12-01 18:15:00', 40.00),
(3, 1, 'Settat', 2, '2026-12-01 19:15:00', 70.00),
(4, 1, 'Marrakech', 3, '2026-12-01 21:30:00', 120.00),
-- Points du Trajet 2 (Casa -> Rabat -> Kenitra -> Tanger)
(5, 2, 'Casablanca', 0, '2026-12-02 08:00:00', 0.00),
(6, 2, 'Rabat', 1, '2026-12-02 09:00:00', 35.00),
(7, 2, 'Kenitra', 2, '2026-12-02 09:40:00', 55.00),
(8, 2, 'Tanger', 3, '2026-12-02 11:30:00', 130.00),
-- Points du Trajet 3 (Fes -> Meknes -> Rabat)
(9, 3, 'Fes', 0, '2026-12-03 09:00:00', 0.00),
(10, 3, 'Meknes', 1, '2026-12-03 09:45:00', 25.00),
(11, 3, 'Rabat', 2, '2026-12-03 11:30:00', 80.00),
-- Points du Trajet 4 (Marrakech -> Chichaoua -> Agadir)
(12, 4, 'Marrakech', 0, '2026-12-04 14:00:00', 0.00),
(13, 4, 'Chichaoua', 1, '2026-12-04 15:00:00', 30.00),
(14, 4, 'Agadir', 2, '2026-12-04 17:00:00', 100.00),
-- Points du Trajet 5 (Safi -> El Jadida -> Casablanca)
(15, 5, 'Safi', 0, '2026-12-05 07:30:00', 0.00),
(16, 5, 'El Jadida', 1, '2026-12-05 09:15:00', 45.00),
(17, 5, 'Casablanca', 2, '2026-12-05 10:30:00', 90.00);

-- 5 Réservations initiales sur les trajets 2, 3, 4, 5 (laissant le Trajet 1 de Hamid libre pour la démo live)
INSERT INTO reservations (id, passager_id, trajet_id, point_montee_id, point_descente_id, nombre_places, montant_total,
                          date_reservation, statut)
VALUES (1, 1, 2, 5, 8, 1, 130.00, '2026-11-16 10:00:00', 'CONFIRMEE'),
       (2, 2, 2, 6, 8, 1, 95.00, '2026-11-16 11:00:00', 'CONFIRMEE'),
       (3, 3, 3, 9, 11, 2, 160.00, '2026-11-16 12:00:00', 'CONFIRMEE'),
       (4, 4, 4, 12, 14, 1, 100.00, '2026-11-16 13:00:00', 'CONFIRMEE'),
       (5, 5, 5, 15, 17, 1, 90.00, '2026-11-16 14:00:00', 'CONFIRMEE');

-- Synchronisation des séquences PostgreSQL après insertion manuelle des IDs
SELECT setval('conducteurs_id_seq', (SELECT MAX(id) FROM conducteurs));
SELECT setval('passagers_id_seq', (SELECT MAX(id) FROM passagers));
SELECT setval('vehicules_id_seq', (SELECT MAX(id) FROM vehicules));
SELECT setval('trajets_id_seq', (SELECT MAX(id) FROM trajets));
SELECT setval('points_arret_id_seq', (SELECT MAX(id) FROM points_arret));
SELECT setval('reservations_id_seq', (  SELECT MAX(id) FROM reservations));
