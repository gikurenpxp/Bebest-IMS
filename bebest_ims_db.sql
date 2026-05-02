-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 02, 2026 at 06:13 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `bebest_ims_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `id` int(11) NOT NULL,
  `barcode` varchar(50) DEFAULT NULL,
  `product_name` varchar(100) NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock_quantity` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`id`, `barcode`, `product_name`, `category`, `price`, `stock_quantity`) VALUES
(3, '48012345', 'Canned Tuna', 'Canned Goods', 35.50, 100),
(34, '101', 'Corned Beef 150g', 'Canned Goods', 45.00, 100),
(35, '102', 'Sardines in Tomato Sauce', 'Canned Goods', 18.50, 250),
(36, '103', 'Canned Peaches in Syrup', 'Canned Goods', 85.00, 40),
(37, '104', 'Mushroom Soup 300ml', 'Canned Goods', 55.00, 60),
(38, '201', 'Jasmine Rice 5kg', 'Food', 290.00, 30),
(39, '202', 'Spaghetti Pasta 500g', 'Food', 42.00, 120),
(40, '203', 'Instant Ramen (Spicy)', 'Food', 15.00, 500),
(41, '204', 'Whole Wheat Bread', 'Food', 65.00, 25),
(42, '301', 'Potato Chips (Salted)', 'Snack', 35.00, 150),
(43, '302', 'Chocolate Bar 50g', 'Snack', 25.00, 200),
(44, '303', 'Mixed Nuts 100g', 'Snack', 75.00, 85),
(45, '304', 'Corn Bits (BBQ)', 'Snack', 12.00, 300),
(46, '401', 'Mineral Water 500ml', 'Beverages', 10.00, 400),
(47, '402', 'Orange Juice 1L', 'Beverages', 95.00, 50),
(48, '403', 'Instant Coffee 3-in-1', 'Beverages', 8.00, 1000),
(49, '404', 'Fresh Milk 1L', 'Beverages', 110.00, 20),
(50, '501', 'Bath Soap (Anti-Bacterial)', 'Toiletries', 38.00, 150),
(51, '502', 'Toothpaste 150g', 'Toiletries', 95.00, 75),
(52, '503', 'Shampoo Bottle 200ml', 'Toiletries', 120.00, 45),
(53, '504', 'Dishwashing Liquid', 'Toiletries', 25.00, 180);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `barcode` (`barcode`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=54;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
