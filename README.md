# AccesslogAnalizer

## prepare

ログファイルを`logs`ディレクトリに入れる。
ファイルは複数でも構わない。

## compile

kotlinc src/*.kt

## run

kotlin MainKt <style-option> <options>

### style-option

- host : アクセスの多いリモートホストの順にアクセス件数の一覧を表示する
- timezone : 各時間帯毎のアクセス件数を表示する
- simple : 総アクセス件数を表示する

### options

- --start yyyy/MM/DD: yyyy年MM月DD日以降のログのみ表示
- --end yyyy/MM/DD: yyyy年MM月DD日までのログのみ表示

### examples

- kotlin MainKt --style host --start 2020/05/01
  - 2020年5月1日以降のログを、アクセスの多いリモートホストの順にアクセス件数の一覧を表示する
- kotlin MainKt --style timezone --start 2020/05/01 --end 2020/05/31
  - 2020年5月1日から2020年5月31日までのログを、各時間帯毎のアクセス件数を表示する
