CREATE TABLE company
(
    id integer NOT NULL,
    name character varying,
    CONSTRAINT company_pkey PRIMARY KEY (id)
);

CREATE TABLE person
(
    id integer NOT NULL,
    name character varying,
    company_id integer references company(id),
    CONSTRAINT person_pkey PRIMARY KEY (id)
);

INSERT INTO company (id, name) VALUES
(1, 'Тех Индастрис'),
(2, 'Инновации Плюс'),
(3, 'Глобал Инк'),
(4, 'Альфа Груп'),
(5, 'Бета и Григорян'),
(6, 'Омега ЛТД');

-- Заполнение таблицы person
INSERT INTO person (id, name, company_id) VALUES
(1, 'Милана Рябкова', 1),
(2, 'Алексей Смирнов', 2),
(3, 'Алиса Демина', 2),
(4, 'Борис Серый', 4),
(5, 'Дмитрий Черкасов', 5),
(6, 'Диана Евакина', 5),
(7, 'Ева Фокина', 6),
(8, 'Федор Зеленый', 3),
(9, 'Григорий Хромов', 3),
(10, 'Герман Ивакин', 5);

-- Имена и название компании за исключением компании id=5
SELECT p.name, c.id, c.name from person p 
	JOIN company c on p.company_id = c.id 
	WHERE p.company_id != 5;

-- Название самой крупной компании и кол-во человек в ней
WITH company_counts AS (
    SELECT 
        c.name AS company_name,
        COUNT(p.id) AS employee_count
    FROM 
        company c
    LEFT JOIN 
        person p ON c.id = p.company_id
    GROUP BY 
        c.name
)
SELECT 
    company_name,
    employee_count
FROM 
    company_counts
WHERE 
    employee_count = (SELECT MAX(employee_count) FROM company_counts);