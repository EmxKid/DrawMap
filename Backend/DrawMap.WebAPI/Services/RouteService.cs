using DrawMap.Domain;
using DrawMap.Repositories;
using DomainRoute = DrawMap.Domain.Route;

namespace DrawMap.WebAPI.Services;

public class RouteService : IRouteService
{
    private readonly IRouteRepository _routeRepository;
    private readonly IPhotoRepository _photoRepository;

    public RouteService(IRouteRepository routeRepository, IPhotoRepository photoRepository)
    {
        _routeRepository = routeRepository;
        _photoRepository = photoRepository;
    }

    public async Task<DomainRoute> AddRoute(DomainRoute route)
    {
        route.Id = Guid.NewGuid().ToString();
        route.Photos ??= [];
        route.Locations ??= [];
        foreach (var location in route.Locations)
            location.Id ??= Guid.NewGuid().ToString();
        await _routeRepository.AddRoute(route);
        return route;
    }

    public async Task<DomainRoute?> GetRoute(string routeId)
    {
        return await _routeRepository.GetRoute(routeId);
    }

    public async Task<bool> DeleteRoute(string routeId)
    {
        await DeletePhotosByRoute(routeId);
        return await _routeRepository.DeleteRoute(routeId);
    }

    public async Task<bool> UpdateRoute(string routeId, DomainRoute route)
    {
        var result = await _routeRepository.UpdateRoute(routeId, route);
        return result is not null;
    }

    private async Task<bool> DeletePhotosByRoute(string routeId)
    {
        var photos = await ListPhotosByRoute(routeId);
        foreach (var photo in photos)
        {
            if (photo.Id is not null)
                await _photoRepository.DeletePhoto(photo.Id);
        }
        return true;
    }

    private async Task<List<Photo>> ListPhotosByRoute(string routeId)
    {
        return await _photoRepository.ListPhotosByRoute(routeId);
    }

    public async Task<Photo> AddPhoto(string routeId, Photo photo)
    {
        photo.Id = Guid.NewGuid().ToString();
        photo.RouteId = routeId;
        await _photoRepository.AddPhoto(photo);
        return photo;
    }
}
