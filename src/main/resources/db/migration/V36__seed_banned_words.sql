INSERT INTO banned_words (word) VALUES
('pelotudo'), ('pelotuda'), ('boludo'), ('boluda'), ('forro'), ('forra'),
('garca'), ('puta'), ('puto'), ('prostituta'), ('mierda'), ('cagada'),
('culo'), ('concha'), ('pija'), ('verga'), ('chota'), ('hdp'),
('hijo de puta'), ('hija de puta'), ('la concha'), ('la puta'),
('carajo'), ('cagón'), ('cagon'), ('inutil'), ('inútil'),
('estúpido'), ('estupido'), ('estúpida'), ('estupida'),
('idiota'), ('imbecil'), ('imbécil'), ('bastardo'), ('bastarda'),
('mogolico'), ('mogólico'), ('retrasado'), ('retrasada'),
('tarado'), ('tarada'), ('cretino'), ('cretina')
ON CONFLICT (word) DO NOTHING;
