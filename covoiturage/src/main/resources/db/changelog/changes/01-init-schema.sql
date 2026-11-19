CREATE TABLE conducteurs
(
    id            BIGSERIAL PRIMARY KEY,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    telephone     VARCHAR(30)  NOT NULL,
    numero_permis VARCHAR(50)  NOT NULL UNIQUE
);

CREATE TABLE passagers
(
    id        BIGSERIAL PRIMARY KEY,
    nom       VARCHAR(100) NOT NULL,
    prenom    VARCHAR(100) NOT NULL,
    email     VARCHAR(150) NOT NULL UNIQUE,
    telephone VARCHAR(30)  NOT NULL
);

CREATE TABLE vehicules
(
    id              BIGSERIAL PRIMARY KEY,
    marque          VARCHAR(100) NOT NULL,
    modele          VARCHAR(100) NOT NULL,
    immatriculation VARCHAR(50)  NOT NULL UNIQUE,
    couleur         VARCHAR(50)  NOT NULL,
    capacite        INTEGER      NOT NULL CHECK (capacite BETWEEN 1 AND 8),
    conducteur_id   BIGINT       NOT NULL REFERENCES conducteurs (id)
);

CREATE TABLE trajets
(
    id               BIGSERIAL PRIMARY KEY,
    conducteur_id    BIGINT       NOT NULL REFERENCES conducteurs (id),
    vehicule_id      BIGINT       NOT NULL REFERENCES vehicules (id),
    ville_depart     VARCHAR(100) NOT NULL,
    ville_arrivee    VARCHAR(100) NOT NULL,
    date_depart      TIMESTAMP    NOT NULL,
    places_proposees INTEGER      NOT NULL,
    statut           VARCHAR(30)  NOT NULL
);

CREATE TABLE points_arret
(
    id                 BIGSERIAL PRIMARY KEY,
    trajet_id          BIGINT         NOT NULL REFERENCES trajets (id) ON DELETE CASCADE,
    ville              VARCHAR(100)   NOT NULL,
    ordre              INTEGER        NOT NULL,
    heure_passage      TIMESTAMP      NOT NULL,
    prix_depuis_depart NUMERIC(10, 2) NOT NULL
);

CREATE TABLE reservations
(
    id                BIGSERIAL PRIMARY KEY,
    passager_id       BIGINT         NOT NULL REFERENCES passagers (id),
    trajet_id         BIGINT         NOT NULL REFERENCES trajets (id),
    point_montee_id   BIGINT         NOT NULL REFERENCES points_arret (id),
    point_descente_id BIGINT         NOT NULL REFERENCES points_arret (id),
    nombre_places     INTEGER        NOT NULL,
    montant_total     NUMERIC(10, 2) NOT NULL,
    date_reservation  TIMESTAMP      NOT NULL,
    statut            VARCHAR(30)    NOT NULL
);