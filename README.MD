# Introdução ao OSGi

## 1. Introdução
Vários aplicativos de missão crítica e middleware em Java têm alguns requisitos tecnológicos rígidos.

Alguns precisam oferecer suporte a hot deploy, para não interromper os serviços em execução - e outros precisam ser capazes de trabalhar com diferentes versões do mesmo pacote para oferecer suporte a sistemas legados externos.

A **Open Service Gateway Initiative** é uma especificação que define um sistema de componentes baseado em Java. Atualmente é gerenciado pela [OSGi Alliance](https://www.osgi.org/), e sua primeira versão data de 1999.

Desde então, provou ser um ótimo padrão para sistemas de componentes e é amplamente utilizado atualmente. O Eclipse IDE, por exemplo, é um aplicativo baseado em OSGi.

Neste artigo, vamos explorar alguns recursos básicos do OSGi aproveitando a implementação fornecida pelo Apache.

## 2. OSGi Básico

**No OSGi, um único componente é chamado de pacote.**

Logicamente, um pacote é uma funcionalidade que possui um ciclo de vida independente - **o que significa que pode ser iniciado, interrompido e removido independentemente**.

Tecnicamente, um pacote é apenas um arquivo jar com um arquivo MANIFEST.MF contendo alguns cabeçalhos específicos do OSGi.

A plataforma OSGi fornece uma maneira de receber notificações sobre pacotes se tornando disponíveis ou quando eles são removidos da plataforma. Isso permitirá que um cliente adequadamente projetado continue trabalhando, talvez com funcionalidade degradada, mesmo quando um serviço do qual ele depende está momentaneamente indisponível.

Por isso, um bundle deve declarar explicitamente a quais pacotes ele precisa ter acesso e a plataforma OSGi irá iniciá-lo somente se as dependências estiverem disponíveis no próprio bundle ou em outros bundles já instalados na plataforma.

## 3. Obtendo as ferramentas

Vamos começar a nossa jornada em OSGi baixando a versão mais recente do Apache Karaf [neste link](https://karaf.apache.org/download.html). Apache Karaf é uma plataforma que executa aplicativos baseados em OSGi. O Karaf baseia-se no [Apache Felix](https://felix.apache.org) que é a implementação da Apache da especificação OSGi.

Karaf oferece alguns recursos úteis em cima do Felix que nos ajudarão a nos familiarizar com o OSGi, por exemplo, uma interface de linha de comando que nos permitirá interagir com a plataforma.

Para instalar o Karaf, você pode seguir as instruções de instalação na [documentação oficial](https://karaf.apache.org/manual/latest/#_quick_start).

## 4. Ponto de entrada do pacote

Para executar um aplicativo em um ambiente OSGi, temos que empacotá-lo como um pacote OSGi e definir o ponto de entrada do aplicativo, e esse não é o método public static void main (String [] args) usual.

**Portanto, vamos começar construindo um aplicativo "Hello World" baseado em OSGi.**

Começamos com a configuração da dependência OSGi:
```
<dependency>
	<groupId>org.osgi</groupId>
	<artifactId>org.osgi.core</artifactId>
	<version>6.0.0</version>
	<scope>provided</scope>
</dependency>
```
A dependência é declarada como **provided** porque estará disponível no tempo de execução OSGi e o pacote configurável não precisa incorporá-lo.

Vamos agora escrever a classe HelloWorld simples:
```
public class HelloWorld implements BundleActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Starting application.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("Stopping application.");
	}	
}
```
**BundleActivator é uma interface fornecida pelo OSGi que deve ser implementada por classes que são pontos de entrada para um pacote.**

O método start () é chamado pela plataforma OSGi quando o pacote que contém essa classe é iniciado. Por outro lado, stop () é invocado antes de o pacote ser interrompido.

Vamos ter em mente que cada pacote pode conter no máximo um BundleActivator. O objeto BundleContext fornecido para ambos os métodos permite interagir com o tempo de execução OSGi.

## 5. Construindo um pacote

Modifique o pom.xml para torná-lo um pacote OSGi real.

Em primeiro lugar, temos que declarar explicitamente que vamos construir um pacote, não um jar:

```
<packaging>bundle</packaging>
```
Em seguida, aproveitamos o maven-bundle-plugin, cortesia da comunidade Apache Felix, para empacotar a classe HelloWorld como um pacote OSGi:
```
<plugin>
    <groupId>org.apache.felix</groupId>
    <artifactId>maven-bundle-plugin</artifactId>
    <version>3.3.0</version>
    <extensions>true</extensions>
    <configuration>
        <instructions>
            <Bundle-SymbolicName>
                ${pom.groupId}.${pom.artifactId}
            </Bundle-SymbolicName>
            <Bundle-Name>${pom.name}</Bundle-Name>
            <Bundle-Version>${pom.version}</Bundle-Version>
            <Bundle-Activator>
                com.baeldung.osgi.sample.activator.HelloWorld
            </Bundle-Activator>
            <Private-Package>
                com.baeldung.osgi.sample.activator
            </Private-Package>            
        </instructions>
    </configuration>
</plugin>
```
Na seção de instruções, especificamos os valores dos cabeçalhos OSGi que desejamos incluir no arquivo MANIFEST do pacote.

Bundle-Activator é o nome totalmente qualificado da implementação BundleActivator que será usada para iniciar e parar o bundle e se refere à classe que acabamos de escrever.

Private-Package não é um cabeçalho OSGi, mas é usado para dizer ao plugin para incluir o pacote no bundle, mas não torná-lo disponível para outros. Agora podemos construir o pacote com o comando usual **mvn clean install**.

## 6. Instalando e executando o pacote

Vamos iniciar o Karaf executando o comando:
```
<KARAF_HOME>/bin/karaf start
```
Onde <KARAF_HOME> é a pasta onde o Karaf está instalado. Quando o prompt do console Karaf aparecer, podemos executar o seguinte comando para instalar o pacote:
```
bundle:install mvn:br.com.codigio/osgi/0.0.1-SNAPSHOT
Bundle ID: 65

Nomenclatura: bundle:install mvn:<group-id>/<artfact-id>/<version>
```
![karaf-install](https://user-images.githubusercontent.com/58561141/97363592-94217300-1881-11eb-9268-5e1f901f7cf2.png)

Isso instrui o Karaf a carregar o pacote do repositório Maven local.

Em troca, o Karaf imprime o ID numérico atribuído ao pacote, que depende do número de pacotes já instalados e pode variar. 

O pacote acabou de ser instalado, agora podemos iniciá-lo com o seguinte comando:
```
> bundle:start 65
Hello world
```

![karaf-start](https://user-images.githubusercontent.com/58561141/97363745-ccc14c80-1881-11eb-8754-e1da3d134cd2.png)

"Starting application" aparece imediatamente assim que o pacote é iniciado. Agora podemos parar e desinstalar o pacote com:
```
> bundle:stop 65
> bundle:uninstall 65
```
![karaf-stop](https://user-images.githubusercontent.com/58561141/97363970-1ca01380-1882-11eb-8b29-82e5607d8482.png)

“Goodbye world” aparece no console, de acordo com o código no método stop() .

## 7. Um serviço OSGi

Vamos continuar escrevendo um serviço OSGi simples , uma interface que expõe um método para saudar as pessoas:

```
package br.com.codigio.service.api;

public interface Greeter {
	public String sayHiTo(String name);
}
```

Vamos escrever uma implementação dele que é um *BundleActivator* também, para que possamos instanciar o serviço e registrá-lo na plataforma quando o pacote for iniciado:

```
package br.com.codigio.service.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import br.com.codigio.service.api.Greeter;

public class GreeterImpl implements Greeter, BundleActivator {
	private ServiceReference<Greeter> reference;
    	private ServiceRegistration<Greeter> registration;
	
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Registering service.");
        registration = context.registerService(
          Greeter.class, 
          new GreeterImpl(), 
          new Hashtable<String, String>());
        reference = registration
          .getReference();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("Unregistering service.");
        registration.unregister();
	}

	@Override
	public String sayHiTo(String name) {		
		return "Hello " + name;
	}
}
```

**Utilizamos o BundleContext como meio de solicitar à plataforma OSGi o registro de uma nova instância do serviço.**

Devemos também fornecer o tipo de serviço e um mapa dos parâmetros de configuração possíveis, que não são necessários em nosso cenário simples. Vamos agora prosseguir com a configuração do plugin maven-bundle:

```
<plugin>
	<groupId>org.apache.felix</groupId>
	<artifactId>maven-bundle-plugin</artifactId>
	<extensions>true</extensions>
	<configuration>
		<instructions>
			<Bundle-SymbolicName>
				${project.groupId}.${project.artifactId}
			</Bundle-SymbolicName>
			<Bundle-Name>
				${project.artifactId}
			</Bundle-Name>
			<Bundle-Version>
				${project.version}
			</Bundle-Version>
			<Bundle-Activator>
				br.com.codigio.service.impl.GreeterImpl
			</Bundle-Activator>
			<Private-Package>
				br.com.codigio.service.impl
			</Private-Package>
			<Export-Package>
				br.com.codigio.service.api
			</Export-Package>
		</instructions>
	</configuration>
</plugin>
```

É importante notar que apenas o pacote *br.com.codigio.service.api* foi exportado desta vez, através do cabeçalho **Export-Package**.

Graças a isso, o OSGi permitirá que outros pacotes invoquem apenas os métodos especificados na interface de serviço. O pacote *br.com.codigio.service.impl* é marcado como privado, portanto, nenhum outro pacote será capaz de acessar os membros da implementação diretamente.

## 8. Um cliente OSGi

Vamos agora escrever o cliente. Ele simplesmente procura o serviço na inicialização e o invoca:

```
public class Client implements BundleActivator, ServiceListener {
}
```

Vamos implementar o método start() de BundleActivator:

```
private BundleContext ctx;
private ServiceReference serviceReference;

@Override
public void start(BundleContext context) throws Exception {
	this.ctx = ctx;
	try {
		ctx.addServiceListener(
		  this, "(objectclass=" + Greeter.class.getName() + ")");
	} catch (InvalidSyntaxException ise) {
		ise.printStackTrace();
	}
}
```

O método addServiceListener() permite que o cliente solicite à plataforma o envio de notificações sobre o serviço que está em conformidade com a expressão fornecida.

A expressão usa uma sintaxe semelhante à do LDAP e, em nosso caso, estamos solicitando notificações sobre um serviço *Greeter*.

Vamos continuar com o método de retorno de chamada:

```
public void serviceChanged(ServiceEvent serviceEvent) {
    int type = serviceEvent.getType();
    switch (type){
        case(ServiceEvent.REGISTERED):
            System.out.println("Notification of service registered.");
            serviceReference = serviceEvent
              .getServiceReference();
            Greeter service = (Greeter)(ctx.getService(serviceReference));
            System.out.println( service.sayHiTo("John") );
            break;
        case(ServiceEvent.UNREGISTERING):
            System.out.println("Notification of service unregistered.");
            ctx.ungetService(serviceEvent.getServiceReference());
            break;
        default:
            break;
    }
}
```

Quando ocorre alguma modificação envolvendo o serviço Greeter, o método é notificado.

Quando o serviço é registrado na plataforma, obtemos uma referência a ele, armazenamos localmente e, em seguida, o usamos para adquirir o objeto de serviço e invocá-lo.

Quando o servidor é posteriormente cancelado, usamos a referência armazenada anteriormente para removê-lo, o que significa que dizemos à plataforma que não iremos mais usá-lo.

Agora só precisamos escrever o método stop():

```
public void stop(BundleContext bundleContext) {
    if(serviceReference != null) {
        ctx.ungetService(serviceReference);
    }
}
```

Aqui, novamente, removemos o serviço para cobrir o caso em que o cliente é interrompido antes de o serviço ser interrompido. Vamos dar uma olhada final nas dependências no *pom.xml*:

```
<dependencies>
	<dependency>
		<groupId>br.com.codigio</groupId>
		<artifactId>osgi-service</artifactId>
		<version>1.0</version>
		<scope>provided</scope>
	</dependency>
	<dependency>
		<groupId>org.osgi</groupId>
		<artifactId>org.osgi.core</artifactId>
		<version>6.0.0</version>
		<scope>provided</scope>
	</dependency>
</dependencies>
```
## 9. Cliente e serviço

Agora vamos instalar os pacotes de cliente e serviço no Karaf fazendo:

```
> bundle:install mvn:br.com.codigio/osgi-service/1.0
Bundle ID: 69
> bundle:install mvn:br.com.codigio/osgi-client/1.0
Bundle ID: 70
```

Sempre tenha em mente que os números identificadores atribuídos a cada pacote podem variar.

Vamos agora iniciar o pacote do cliente:

```
> start 70
```

Portanto, nada acontece porque o cliente está ativo e está aguardando o serviço, que podemos começar com:

```
> start 69
Registering service.
Service registered.
Hello John
```

O que acontece é que assim que o BundleActivator do serviço é iniciado, o serviço é registrado na plataforma. Isso, por sua vez, notifica o cliente de que o serviço que estava esperando está disponível.

O cliente então obtém uma referência ao serviço e a usa para invocar a implementação fornecida por meio do pacote de serviço.

## 10. Conclusão

Neste artigo, exploramos os recursos essenciais do OSGi com um exemplo simples de que é o suficiente para entender o potencial do OSGi.

Concluindo, sempre que temos que garantir que um único aplicativo seja atualizado sem qualquer prejuízo, o OSGi pode ser uma solução viável.
