-- Migration: Add per-question answer tracking to quiz_results
-- Purpose: Store user's selected answer for each question to enable detailed result review
-- This allows the app to compare user answers against correct answers and display detailed feedback

ALTER TABLE quiz_results ADD COLUMN answers_json TEXT DEFAULT '[]';

-- answers_json stores a JSON array of 1-based answer indices, one per question
-- Example: "[2,1,3,4,2,1]" means user selected option 2 for Q1, option 1 for Q2, etc.
-- Empty array (default) indicates legacy results before this column was added
