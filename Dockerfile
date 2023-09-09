FROM test_java
WORKDIR /mylouise
EXPOSE 8099/tcp
EXPOSE 6380/tcp
COPY ./target /mylouise
CMD ["sh", "./boot.sh"]