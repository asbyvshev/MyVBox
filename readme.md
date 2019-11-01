# Сетевое хранилище

Сетевое хранилище позволяет перекидывать файлы между клиентом и сервером.
Основные возможности: аутентификация пользователей, отправка файлов на сервер, 
скачивание файлов с сервера, удаление файлов на клиенте/сервере, 
переименование файл на клиенте/сервере, (* регистрация пользователей)

### Основные вопросы/проблемы:
1. Как вы собираетесь передавать файлы?
Сериализация

2. Где вы собираетесь их хранить?
Для каждого пользователя будет создана папка с его именем, туда файлы и закидывает.
Делать многоуровневую структуру этого каталога только после выполнения основной части.

3. Нужна ли база данных и зачем?
Да в БД храним:
- логин\пароль
- Список файлов пользователя

4. Что вы хотите передавать помимо файлов?
Какие-то команды? (данные для регистрации, данные для аутентификации, команда на удаление 
файла на сервере, запрос на скачивание файла и т.д.). Как вы собираетесь это делать параллельно 
с файлами?

5. Библиотеки для работы?
java.io, java.nio, Netty

### \* Продвинутые возможности:
1. Проверка контрольной суммы 
2. Синхронизация файлов на клиенте/сервере
3. Шифрование
4. Отображение дерева каталогов на сервере
5. Ограничение объекма хранилища для отдельных клиентов
6. Многопоточная загрузка
7. Догрузка файлов после обрыва соединения
8. "Расшаривание" файлов

### Заметки:
1. Лимит по объему файлов: не нужен
2. Передаются любые типы файлов

### Выполненые задачи:
1. Скачивание файла с сервера на клиент.
2. Загрузка файла с клиента на сервер.

- Отображение дерева каталогов на сервере

- добавить в gui пользователя информацию:
 - о содержимом сервера
 - кнопки удаления\переименования(на сервере\клиенте) + реализовать этот функцианал

- авторизация

- регистрация нового аользователя
    - создание лог\пас
    - создание отдельного каталога для пользователя на сервере

- оптимизация отправки больших файлов