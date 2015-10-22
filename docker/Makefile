IMAGE_MAIN_NAME='clouddev/spark-kafka-sdk'
IMAGE_VERSION='1.0'
IMAGE=$(IMAGE_MAIN_NAME):$(IMAGE_VERSION)

REGISTRY='registry.docker.dev.sogou-inc.com:5000'

ifdef NO_CACHE
    BUILD_PARAM='--no-cache=true'
else
    BUILD_PARAM=
endif

all: build

clean:
	build/sbt clean; rm -fr dist

dist:
	cd ..; bin/release-tgz.sh; cd -
	
build: dist
	tar -xzvf ../dist/spark-kafka-sdk_2.10.4-1.0.tgz -C .
	docker build $(BUILD_PARAM) -t $(IMAGE_MAIN_NAME) .
	rm -fr spark-kafka-sdk_2.10.4-1.0

push: build
	docker tag -f $(IMAGE_MAIN_NAME) $(REGISTRY)/$(IMAGE) && docker push $(REGISTRY)/$(IMAGE)
