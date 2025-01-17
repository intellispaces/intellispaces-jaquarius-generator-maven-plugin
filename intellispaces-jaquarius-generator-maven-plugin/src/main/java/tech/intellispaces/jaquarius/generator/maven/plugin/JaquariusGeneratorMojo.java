package tech.intellispaces.jaquarius.generator.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import tech.intellispaces.general.collection.ArraysFunctions;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.data.Dictionary;
import tech.intellispaces.general.exception.NotImplementedExceptions;
import tech.intellispaces.general.text.StringFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.ConfigurationLoaderFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Settings;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.SettingsProvider;
import tech.intellispaces.jaquarius.generator.maven.plugin.generation.GenerationFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.DirectOntologyRepository;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.OntologyRepository;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationReadFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.UnitedOntologyRepository;
import tech.intellispaces.jaquarius.space.domain.PrimaryDomainFunctions;
import tech.intellispaces.jaquarius.space.domain.PrimaryDomainSet;
import tech.intellispaces.jaquarius.space.domain.PrimaryDomains;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mojo(
    name = "jaquarius-generator",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class JaquariusGeneratorMojo extends AbstractMojo {

  /**
   * The specification file path.
   */
  @Parameter(property = "inputSpec", required = true)
  private String inputSpec;

  /**
   * The external ontology repositories.
   */
  @Parameter(property = "repositories", required = false)
  private String[] repositories;

  /**
   * The directory for generated Java source files.
   */
  @Parameter(
      property = "outputDirectory",
      defaultValue = "${project.build.directory}/generated-sources/jaquarius",
      required = true)
  private String outputDirectory;

  @Parameter(defaultValue = "${project}", required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      Settings settings = createSettings();

      var unitedRepository = new UnitedOntologyRepository();
      Configuration cfg = createConfiguration(settings, unitedRepository);

      customizeJaquariusSettings();

      Path specPath = Paths.get(cfg.settings().specificationPath());
      Specification spec = SpecificationReadFunctions.readSpecification(specPath, cfg);
      unitedRepository.addProvider(new DirectOntologyRepository(spec));

      addOntologyRepositories(unitedRepository, cfg);

      GenerationFunctions.generateArtifacts(spec, cfg);

      project.addCompileSourceRoot(cfg.settings().outputDirectory());
    } catch (MojoExecutionException e) {
      getLog().error("Failed to execute plugin", e);
      throw e;
    } catch (Exception e) {
      getLog().error("Unexpected exception", e);
      throw new MojoExecutionException("Unexpected exception", e);
    }
  }

  Configuration createConfiguration(
      Settings settings,
      OntologyRepository repository
  ) throws MojoExecutionException {
    return ConfigurationLoaderFunctions.loadConfiguration(
        settings,
        repository,
        getLog()
    );
  }

  Settings createSettings() {
    return SettingsProvider.builder()
        .projectPath(project.getBasedir().toString())
        .specificationPath(inputSpec)
        .outputDirectory(outputDirectory)
        .get();
  }

  void addOntologyRepositories(
      UnitedOntologyRepository unitedRepository, Configuration cfg
  ) throws MojoExecutionException {
    if (ArraysFunctions.isNullOrEmpty(repositories)) {
      return;
    }
    for (String repositoryUrl : repositories) {
      if (repositoryUrl.startsWith("file://")) {
        addFileOntologyRepository(unitedRepository, repositoryUrl, cfg);
      } else {
        throw NotImplementedExceptions.withCode("WkYWoTxe");
      }
    }
  }

  void addFileOntologyRepository(
      UnitedOntologyRepository unitedRepository, String repositoryUrl, Configuration cfg
  ) throws MojoExecutionException {
      var specPath = Path.of(StringFunctions.removeHead(URI.create(repositoryUrl).getPath(), "/"));
      Specification spec = SpecificationReadFunctions.readSpecification(specPath, cfg);
      unitedRepository.addProvider(new DirectOntologyRepository(spec));
  }

  void customizeJaquariusSettings() throws MojoExecutionException {
    PrimaryDomainSet primaryDomains = readPrimaryDomains();
    PrimaryDomains.current(primaryDomains);
  }

  PrimaryDomainSet readPrimaryDomains() throws MojoExecutionException {
    var dictionaries = new ArrayList<Dictionary>();

    // Try to direct read
    try {
      dictionaries.add(PrimaryDomainFunctions.readPrimaryDomainDictionary(project.getBasedir().toString()));
    } catch (IOException e) {
      // ignore
    }

    // Try to read from classpath
    try {
      dictionaries.addAll(PrimaryDomainFunctions.readPrimaryDomainDictionaries(projectClassLoader()));
    } catch (Exception e) {
      throw new MojoExecutionException("Could not to load file domain.properties", e);
    }
    return PrimaryDomains.get(dictionaries);
  }

  @SuppressWarnings("unchecked")
  ClassLoader projectClassLoader() throws MojoExecutionException {
    try {
      List<URL> urls = CollectionFunctions.mapEach(
          (Set<Artifact>) project.getDependencyArtifacts(), a -> a.getFile().toURI().toURL());
      return new URLClassLoader(urls.toArray(new URL[0]), ConfigurationLoaderFunctions.class.getClassLoader());
    } catch (MalformedURLException e) {
      throw new MojoExecutionException("Could not get project classloader", e);
    }
  }
}
