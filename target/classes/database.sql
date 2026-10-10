-- ===================================================================
-- Travel Package Booking System (TravelGo)
-- Database Creation & Seed Data Script
-- ===================================================================

CREATE DATABASE IF NOT EXISTS travel_booking;
USE travel_booking;

DROP TABLE IF EXISTS booking_travelers;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS travel_packages;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE travel_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    description TEXT,
    duration VARCHAR(50) NOT NULL,
    price DOUBLE NOT NULL,
    discount_percent DOUBLE NOT NULL DEFAULT 0,
    family_friendly BOOLEAN NOT NULL DEFAULT FALSE,
    group_trip BOOLEAN NOT NULL DEFAULT FALSE,
    places TEXT,
    place_images TEXT,
    place_image_sources TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    package_id BIGINT NOT NULL,
    travel_date VARCHAR(50) NOT NULL,
    number_of_persons INT NOT NULL,
    total_amount DOUBLE NOT NULL,
    staff_in_charge_id BIGINT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_package FOREIGN KEY (package_id) REFERENCES travel_packages(id),
    CONSTRAINT fk_booking_staff_in_charge FOREIGN KEY (staff_in_charge_id) REFERENCES users(id)
);

CREATE TABLE booking_travelers (
    booking_id BIGINT NOT NULL,
    traveler_order INT NOT NULL,
    traveler_name VARCHAR(100) NOT NULL,
    traveler_email VARCHAR(100) NOT NULL,
    traveler_phone VARCHAR(20) NOT NULL,
    PRIMARY KEY (booking_id, traveler_order),
    CONSTRAINT fk_booking_traveler_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

INSERT INTO users (name, email, phone, password, role) VALUES
('System Administrator', 'admin@travelgo.com', '9999999999', 'admin123', 'ADMIN'),
('John Doe', 'john@example.com', '9876543210', 'user123', 'USER');

INSERT INTO travel_packages (name, destination, description, duration, price, places, place_images, place_image_sources) VALUES
('Goa Beach Escape', 'Goa', 'Enjoy the golden sands, vibrant beach shacks, water sports, and Portuguese architecture in North and South Goa.', '3 Days', 8999.00, 'Baga Beach\nFort Aguada\nBasilica of Bom Jesus\nAnjuna Flea Market', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/80/Baga_Beach_25012016.jpg/960px-Baga_Beach_25012016.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/9/95/Fort_Aguada_Remote_view_25012016.jpg/960px-Fort_Aguada_Remote_view_25012016.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/d/de/Side_Elevation_of_Basilica_of_Bom_Jesus.jpg/960px-Side_Elevation_of_Basilica_of_Bom_Jesus.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/1/1b/Anjuna%2C_Goa%2C_India%2C_Anjuna_Flea_Market_2.jpg/960px-Anjuna%2C_Goa%2C_India%2C_Anjuna_Flea_Market_2.jpg', 'https://commons.wikimedia.org/wiki/File:Baga_Beach_25012016.jpg\nhttps://commons.wikimedia.org/wiki/File:Fort_Aguada_Remote_view_25012016.jpg\nhttps://commons.wikimedia.org/wiki/File:Side_Elevation_of_Basilica_of_Bom_Jesus.jpg\nhttps://commons.wikimedia.org/wiki/File:Anjuna,_Goa,_India,_Anjuna_Flea_Market_2.jpg'),
('Manali Adventure', 'Manali', 'Experience snow valleys, Solang valley sports, Rohtang Pass, and scenic pine-covered Himalayan trails.', '5 Days', 14999.00, 'Hadimba Devi Temple\nSolang Valley\nAtal Tunnel\nOld Manali', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/28/Hadimba_Devi_Temple_Manali.jpg/960px-Hadimba_Devi_Temple_Manali.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/b/b5/Solang_Valley%2C_Manali.jpg/960px-Solang_Valley%2C_Manali.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/PM_Narendra_Modi_stands_at_the_entrance_to_the_Atal_Tunnel_in_Rohtang.jpg/960px-PM_Narendra_Modi_stands_at_the_entrance_to_the_Atal_Tunnel_in_Rohtang.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/1/1a/Old_Manali%2C_Manali%2C_India_%28Unsplash_MSK-2kuYJDE%29.jpg/960px-Old_Manali%2C_Manali%2C_India_%28Unsplash_MSK-2kuYJDE%29.jpg', 'https://commons.wikimedia.org/wiki/File:Hadimba_Devi_Temple_Manali.jpg\nhttps://commons.wikimedia.org/wiki/File:Solang_Valley,_Manali.jpg\nhttps://commons.wikimedia.org/wiki/File:PM_Narendra_Modi_stands_at_the_entrance_to_the_Atal_Tunnel_in_Rohtang.jpg\nhttps://commons.wikimedia.org/wiki/File:Old_Manali,_Manali,_India_(Unsplash_MSK-2kuYJDE).jpg'),
('Kerala Backwaters', 'Kerala', 'Relax on a tranquil houseboat cruise across Alleppey backwaters, tea plantations of Munnar, and spice gardens.', '4 Days', 11999.00, 'Alleppey Backwaters\nMunnar Tea Gardens\nMattupetty Dam\nFort Kochi', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d5/Alleppey_backwaters_view_%2C_Kerala_%2C_India_15.jpg/960px-Alleppey_backwaters_view_%2C_Kerala_%2C_India_15.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/3/38/Munnar_tea_gardens.jpg/960px-Munnar_tea_gardens.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/a/af/Mattupetty_Lake_View.jpg/960px-Mattupetty_Lake_View.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Chinese_Fishing_Nets_-_2.jpg/960px-Chinese_Fishing_Nets_-_2.jpg', 'https://commons.wikimedia.org/wiki/File:Alleppey_backwaters_view_,_Kerala_,_India_15.jpg\nhttps://commons.wikimedia.org/wiki/File:Munnar_tea_gardens.jpg\nhttps://commons.wikimedia.org/wiki/File:Mattupetty_Lake_View.jpg\nhttps://commons.wikimedia.org/wiki/File:Chinese_Fishing_Nets_-_2.jpg'),
('Hyderabad Heritage Tour', 'Hyderabad', 'Explore the historic Charminar, Golconda Fort, Salar Jung Museum, and savor authentic Hyderabadi Biryani.', '2 Days', 5999.00, 'Charminar\nGolconda Fort\nSalar Jung Museum\nChowmahalla Palace', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/71/Charminar_Hyderabad_1.jpg/960px-Charminar_Hyderabad_1.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/1/12/Golconda_Fort%2C_Hyderabad_%28Oct_2025%29_-_3.jpg/960px-Golconda_Fort%2C_Hyderabad_%28Oct_2025%29_-_3.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Salar_Jung_Museum%2C_the_building_and_complex_in_Hyderabad_21.jpg/960px-Salar_Jung_Museum%2C_the_building_and_complex_in_Hyderabad_21.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/Chowmahalla_Palace_gate_tower_01.jpg/960px-Chowmahalla_Palace_gate_tower_01.jpg', 'https://commons.wikimedia.org/wiki/File:Charminar_Hyderabad_1.jpg\nhttps://commons.wikimedia.org/wiki/File:Golconda_Fort,_Hyderabad_(Oct_2025)_-_3.jpg\nhttps://commons.wikimedia.org/wiki/File:Salar_Jung_Museum,_the_building_and_complex_in_Hyderabad_21.jpg\nhttps://commons.wikimedia.org/wiki/File:Chowmahalla_Palace_gate_tower_01.jpg'),
('Jaipur Royal Trip', 'Jaipur', 'Witness the regal magnificence of Amber Fort, Hawa Mahal, City Palace, and vibrant Rajasthani cultural bazaars.', '4 Days', 10999.00, 'Amber Fort\nHawa Mahal\nCity Palace\nJantar Mantar', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f7/Jaipur_03-2016_02_Amber_Fort.jpg/960px-Jaipur_03-2016_02_Amber_Fort.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/4/41/East_facade_Hawa_Mahal_Jaipur_from_ground_level_%28July_2022%29_-_img_01.jpg/960px-East_facade_Hawa_Mahal_Jaipur_from_ground_level_%28July_2022%29_-_img_01.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/4/40/Jaipur_03-2016_20_City_Palace_complex.jpg/960px-Jaipur_03-2016_20_City_Palace_complex.jpg\nhttps://thumb.wikimedia.org/wikipedia/commons/thumb/8/80/Doors_of_Jantar_Mantar.jpg/960px-Doors_of_Jantar_Mantar.jpg', 'https://commons.wikimedia.org/wiki/File:Jaipur_03-2016_02_Amber_Fort.jpg\nhttps://commons.wikimedia.org/wiki/File:East_facade_Hawa_Mahal_Jaipur_from_ground_level_(July_2022)_-_img_01.jpg\nhttps://commons.wikimedia.org/wiki/File:Jaipur_03-2016_20_City_Palace_complex.jpg\nhttps://commons.wikimedia.org/wiki/File:Doors_of_Jantar_Mantar.jpg');

INSERT INTO bookings (user_id, package_id, travel_date, number_of_persons, total_amount, status) VALUES
(2, 1, '2026-11-15', 2, 17998.00, 'CONFIRMED');

INSERT INTO booking_travelers (booking_id, traveler_order, traveler_name, traveler_email, traveler_phone) VALUES
(1, 0, 'John Doe', 'john@example.com', '9876543210'),
(1, 1, 'Jane Doe', 'jane@example.com', '9876543211');
