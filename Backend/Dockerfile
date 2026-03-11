# Этап сборки
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Копируем файлы проектов для восстановления зависимостей
COPY DrawMap.Domain/DrawMap.Domain.csproj             DrawMap.Domain/
COPY DrawMap.DataSources/DrawMap.DataSources.csproj   DrawMap.DataSources/
COPY DrawMap.Repositories/DrawMap.Repositories.csproj DrawMap.Repositories/
COPY DrawMap.WebAPI/DrawMap.WebAPI.csproj             DrawMap.WebAPI/

RUN dotnet restore DrawMap.WebAPI/DrawMap.WebAPI.csproj

# Копируем весь исходный код и публикуем
COPY . .
RUN dotnet publish DrawMap.WebAPI/DrawMap.WebAPI.csproj \
    -c Release \
    -o /app/publish \
    --no-restore

# Этап выполнения
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime
WORKDIR /app

COPY --from=build /app/publish .

ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production

EXPOSE 8080

ENTRYPOINT ["dotnet", "DrawMap.WebAPI.dll"]
