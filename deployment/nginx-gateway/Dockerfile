FROM nginx:1.27-alpine
# remove the image's default vhost
RUN rm -f /etc/nginx/conf.d/default.conf \
 && mkdir -p /run/nginx
COPY ./conf/* /etc/nginx/conf.d/
COPY ./backends/ /etc/nginx/backends/
EXPOSE 80
