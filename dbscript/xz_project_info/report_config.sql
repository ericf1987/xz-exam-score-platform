DROP TABLE IF EXISTS report_config;
CREATE TABLE report_config(
  project_id VARCHAR(36) primary key,
  combine_category_subjects VARCHAR(5) NOT NULL DEFAULT 'false',
  separate_category_subjects VARCHAR(5) NOT NULL DEFAULT 'false',
  college_entry_level_enabled VARCHAR(5) NOT NULL DEFAULT 'false',
  rank_levels TEXT,
  rank_segment_count INT,
  score_levels TEXT,
  rank_level_combines TEXT,
  top_student_rate DECIMAL(5,2),
  high_score_rate DECIMAL(5,2),
  college_entry_level TEXT,
  entry_level_stat_type VARCHAR(20),
  share_school_report VARCHAR(5) NOT NULL DEFAULT 'false'
);
