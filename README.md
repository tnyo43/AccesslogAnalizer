# AccesslogAnalizer

## prepare

ログファイルを`logs`ディレクトリに入れる。
ファイルは複数でも構わない。

## compile

kotlinc src/*.kt

## run

kotlin MainKt <option>

### options

- -h : アクセスの多いリモートホストの順にアクセス件数の一覧を表示する
- -t : 各時間帯毎のアクセス件数を表示する
- -s : 総アクセス件数を表示する
