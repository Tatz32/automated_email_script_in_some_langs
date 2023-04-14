import pandas as pd
import mysql.connector

# CSVファイルを読み込む
file_path = "filepath_to_the_csv"
df = pd.read_csv(file_path)

# MySQLデータベースに接続する
connection = mysql.connector.connect(
    host="localhost",
    user="my_sql_username",
    password="my_sql_pass",
    database="export_test_database"
)

# カーソルを作成する
cursor = connection.cursor()

# テーブルを作成する (既存のテーブルがある場合は削除してください)
table_name = "Email_database"
create_table_query = f"CREATE TABLE {table_name} ("
for column_name, dtype in zip(df.columns, df.dtypes):
    if "int" in str(dtype):
        column_type = "INT"
    elif "float" in str(dtype):
        column_type = "FLOAT"
    else:
        column_type = "VARCHAR(255)"
    create_table_query += f"`{column_name}` {column_type}, "
create_table_query = create_table_query.strip(", ") + ");"
cursor.execute(create_table_query)

# データをテーブルに挿入する
for _, row in df.iterrows():
    insert_query = f"INSERT INTO {table_name} ({', '.join([f'`{col}`' for col in df.columns])}) VALUES ({', '.join(['%s'] * len(df.columns))})"
    cursor.execute(insert_query, tuple(row))

# 変更をコミットし、接続を閉じる
connection.commit()
cursor.close()
connection.close()
