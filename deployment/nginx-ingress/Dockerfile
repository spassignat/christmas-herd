FROM nginx:1.27-alpine
# remove the image's default vhost (server_name localhost) which shadows ours
RUN rm -f /etc/nginx/conf.d/default.conf
COPY ./conf/* /etc/nginx/conf.d/
EXPOSE 80
