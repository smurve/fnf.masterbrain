FROM ubuntu:14.04

MAINTAINER Thomas Scheuchzer <thomas.scheuchzer@gmx.net>



# Install latest R
RUN echo 'deb http://www.laqee.unal.edu.co/CRAN/bin/linux/ubuntu trusty/' >> /etc/apt/sources.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys E084DAB9
RUN apt-get update && apt-get install -y --force-yes r-base r-base-dev

# Important additional libraries
RUN apt-get install -y libcurl4-gnutls-dev libxml2-dev libmime-base64-urlsafe-perl libdigest-hmac-perl libdigest-sha-perl libcairo2 libcairo2-dev xvfb xauth xfonts-base libxt-dev

ENV CRAN_REPO http://cran.rstudio.com/

# The devtools enable the installation of packages hosted on github.
RUN R -e "install.packages(c('Rserve', 'devtools', 'Cairo'), repos='$CRAN_REPO')"
RUN R -e "install.packages(c('knitr'), repos='$CRAN_REPO')"
RUN R -e "install.packages(c('ggplot2', 'plyr', 'gridExtra', 'RJSONIO', 'reshape2', 'plotrix'), repos='$CRAN_REPO')"



VOLUME /rserv

ADD start.R /rserv/start.R
ADD Rusers.conf /rserv/Rusers.conf
ADD Rserv.conf /rserv/Rserv.conf


EXPOSE 6311

USER nobody

CMD Rscript /rserv/start.R

