-- Creazione delle tabelle principali per l'applicazione

-- Tabella degli utenti
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- Tabella dei ruoli
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Tabella di relazione many-to-many tra utenti e ruoli
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Tabella delle cartelle
CREATE TABLE folders (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    parent_folder_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (parent_folder_id) REFERENCES folders (id) ON DELETE CASCADE
);

-- Tabella dei file
CREATE TABLE stored_files (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    folder_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folders (id) ON DELETE CASCADE
);

-- Tabella dei permessi di condivisione
CREATE TABLE share_permissions (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    shared_with_user_id BIGINT,
    permission_type VARCHAR(20) NOT NULL,
    is_public_link BOOLEAN NOT NULL,
    public_link_token VARCHAR(255) UNIQUE,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (shared_with_user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Inserimento del ruolo base
INSERT INTO roles (name) VALUES ('ROLE_USER');

-- Indici per migliorare le prestazioni delle query più frequenti
CREATE INDEX idx_stored_files_owner_id ON stored_files (owner_id);
CREATE INDEX idx_stored_files_folder_id ON stored_files (folder_id);
CREATE INDEX idx_folders_owner_id ON folders (owner_id);
CREATE INDEX idx_folders_parent_folder_id ON folders (parent_folder_id);
CREATE INDEX idx_share_permissions_resource ON share_permissions (resource_id, resource_type);
CREATE INDEX idx_share_permissions_user ON share_permissions (shared_with_user_id);
CREATE INDEX idx_share_permissions_token ON share_permissions (public_link_token);