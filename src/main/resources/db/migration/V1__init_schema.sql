-- USERS
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    university_email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('buyer', 'seller', 'admin') DEFAULT 'buyer',
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- CATEGORIES
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN DEFAULT TRUE
);

-- LISTINGS
CREATE TABLE listings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    category_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    status ENUM('available','sold','inactive') DEFAULT 'available',
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_listing_seller
        FOREIGN KEY (seller_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_listing_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
);

-- LISTING IMAGES
CREATE TABLE listing_images (
    id INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT DEFAULT 1,

    CONSTRAINT fk_listing_image
        FOREIGN KEY (listing_id)
        REFERENCES listings(id)
        ON DELETE CASCADE
);

-- CONVERSATIONS
CREATE TABLE conversations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    buyer_id INT NOT NULL,
    seller_id INT NOT NULL,

    CONSTRAINT fk_conv_listing
        FOREIGN KEY (listing_id)
        REFERENCES listings(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_conv_buyer
        FOREIGN KEY (buyer_id)
        REFERENCES users(id),

    CONSTRAINT fk_conv_seller
        FOREIGN KEY (seller_id)
        REFERENCES users(id)
);

-- MESSAGES
CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conversation_id INT NOT NULL,
    sender_id INT NOT NULL,
    message_text TEXT NOT NULL,
    read_at TIMESTAMP NULL,

    CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id)
        REFERENCES users(id)
);

-- ORDERS
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    buyer_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending','paid','completed','cancelled') DEFAULT 'pending',
    payment_method VARCHAR(50),

    CONSTRAINT fk_order_listing
        FOREIGN KEY (listing_id)
        REFERENCES listings(id),

    CONSTRAINT fk_order_buyer
        FOREIGN KEY (buyer_id)
        REFERENCES users(id)
);

-- PAYMENTS
CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending','success','failed','refunded') DEFAULT 'pending',
    provider_reference VARCHAR(255),
    idempotency_key VARCHAR(255) UNIQUE,

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE
);

-- REVIEWS
CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    reviewer_id INT NOT NULL,
    reviewee_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,

    CONSTRAINT fk_review_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_review_reviewer
        FOREIGN KEY (reviewer_id)
        REFERENCES users(id),

    CONSTRAINT fk_review_reviewee
        FOREIGN KEY (reviewee_id)
        REFERENCES users(id)
);

-- NOTIFICATIONS
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(50),
    title VARCHAR(255),
    body TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
