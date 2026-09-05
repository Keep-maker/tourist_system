# -*- coding: utf-8 -*-
"""
读 全国景点数据.csv 生成 SQL 导入文件
- 拆分 region 得到 省/市/区县
- score 0-1 → 0-5 (×5)
- level(5A景区) → spot_type(字符串) + star_level(数字)
- comments → spot_intro
"""
import csv

CSV_PATH = r'd:\tourism-system\全国景点数据.csv'
SQL_OUT = r'd:\tourism-system\dataset-backup\import_new_data.sql'

def esc(s):
    """SQL 字符串转义: 单引号→双单引号, None→空串"""
    if s is None:
        return ''
    return str(s).replace("'", "''").replace("\\", "\\\\")

def parse_level(level):
    """从 '5A景区' 提取数字 5; 提取不到返回 None"""
    if not level:
        return None
    for ch in level:
        if ch.isdigit():
            return int(ch)
    return None

# 1. 读 CSV
rows = []
with open(CSV_PATH, 'r', encoding='utf-8-sig') as f:
    reader = csv.DictReader(f)
    for r in reader:
        rows.append(r)
print(f'CSV 读取完成: {len(rows)} 条')

# 2. 收集城市去重 (province, city)
city_set = {}
for r in rows:
    region = r.get('region', '') or ''
    parts = region.split('·')
    province = parts[0].strip() if len(parts) >= 1 else ''
    city = parts[1].strip() if len(parts) >= 2 else province
    if province and city:
        city_set[(province, city)] = True
print(f'去重城市数: {len(city_set)}')

# 3. 生成 SQL
sql_lines = []
sql_lines.append("SET NAMES utf8mb4;")
sql_lines.append("SET FOREIGN_KEY_CHECKS=0;")
# 坐标文本字段新数据集没有, 改为允许NULL(存0是脏数据)
sql_lines.append("ALTER TABLE scenic_spot MODIFY coordinate varchar(50) NULL COMMENT '坐标文本(新数据集无,允许空)';")
sql_lines.append("TRUNCATE TABLE scenic_spot;")
sql_lines.append("TRUNCATE TABLE scenic_city;")

# 3.1 插入城市
for (prov, city) in city_set:
    sql_lines.append(
        f"INSERT INTO scenic_city (province, city) "
        f"VALUES ('{esc(prov)}', '{esc(city)}');"
    )

# 3.2 插入景点: 用子查询关联城市拿 city_id
ok_cnt = 0
skip_cnt = 0
for r in rows:
    region = r.get('region', '') or ''
    parts = region.split('·')
    province = parts[0].strip() if len(parts) >= 1 else ''
    city = parts[1].strip() if len(parts) >= 2 else province
    district = parts[2].strip() if len(parts) >= 3 else ''

    name = r.get('name', '') or ''
    if not name.strip():
        skip_cnt += 1
        continue

    score_raw = r.get('score', '') or ''
    try:
        score = round(float(score_raw) * 5, 2)  # 0-1 → 0-5
    except ValueError:
        score = 0

    level = r.get('level', '') or ''
    # 数据集用 \N 表示无等级, 当作空处理
    if level.strip() in ('', '\\N', 'N', 'NULL', 'null', 'NaN', 'nan'):
        level = ''
    star_level = parse_level(level)
    if star_level is None:
        star_level = 3  # level 提取不到数字时默认3星
    spot_type = level if level else '未评级'  # spot_type 存 level 字符串,空值给默认

    address = r.get('address', '') or ''
    comments = r.get('comments', '') or ''
    price_raw = r.get('price', '') or ''
    sales_raw = r.get('sales', '') or ''
    try:
        price = float(price_raw) if price_raw != '' else 0
    except ValueError:
        price = 0
    try:
        sales = float(sales_raw) if sales_raw != '' else 0
    except ValueError:
        sales = 0

    if price < 0:
        skip_cnt += 1
        continue

    star_val = str(star_level) if star_level is not None else 'NULL'

    sql_lines.append(
        f"INSERT INTO scenic_spot "
        f"(city_id, district, spot_name, spot_type, score, ticket_price, sales_volume, "
        f"coordinate, spot_intro, address, star_level) "
        f"VALUES ((SELECT id FROM scenic_city WHERE province='{esc(province)}' "
        f"AND city='{esc(city)}' LIMIT 1), '{esc(district)}', '{esc(name)}', "
        f"'{esc(spot_type)}', {score}, {price}, {sales}, NULL, '{esc(comments)}', "
        f"'{esc(address)}', {star_val});"
    )
    ok_cnt += 1

sql_lines.append("SET FOREIGN_KEY_CHECKS=1;")

with open(SQL_OUT, 'w', encoding='utf-8') as f:
    f.write('\n'.join(sql_lines))

import os
print(f'SQL 文件已生成: {SQL_OUT}')
print(f'文件大小: {os.path.getsize(SQL_OUT)/1024/1024:.2f} MB')
print(f'成功: {ok_cnt} 条, 跳过(脏数据): {skip_cnt} 条')
