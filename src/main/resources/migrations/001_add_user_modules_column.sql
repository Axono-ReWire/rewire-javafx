-- Migration: Add module_name column to user_modules table
-- This migration fixes the schema for signup/onboarding module persistence
-- Applied: 2026-05-23

ALTER TABLE user_modules ADD COLUMN module_name TEXT NOT NULL DEFAULT '';
